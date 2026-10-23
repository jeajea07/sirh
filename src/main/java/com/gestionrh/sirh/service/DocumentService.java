package com.gestionrh.sirh.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.model.DocumentModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.OnboardingTacheModel;
import com.gestionrh.sirh.model.UtilisateurModel;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.DocumentRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.OnboardingTacheRepository;
import com.gestionrh.sirh.repository.UtilisateurRepository;
import com.gestionrh.sirh.service.dto.DocumentForm;
import com.gestionrh.sirh.service.dto.DocumentResume;
import com.gestionrh.sirh.util.Formats;

@Service
public class DocumentService {

    private static final long TAILLE_MAX = 10L * 1024 * 1024;
    private static final Set<String> EXTENSIONS = Set.of("pdf", "docx", "doc", "png", "jpg", "jpeg");
    private static final Set<String> TYPES = Set.of("CONTRAT", "ADMINISTRATIF", "SOCIAL", "SANTE",
            "INTERNE", "FORMATION", "MATERIEL", "CV");
    private static final Set<String> CONFIDENTIALITES = Set.of("PUBLIC", "INTERNE", "CONFIDENTIEL", "SECRET");
    private static final DateTimeFormatter DOSSIER = DateTimeFormatter.ofPattern("yyyy/MM");

    private final DocumentRepository documents;
    private final EmployeRepository employes;
    private final ContratRepository contrats;
    private final OnboardingTacheRepository taches;
    private final UtilisateurRepository utilisateurs;
    private final AuditService audit;
    private final String uploadDir;

    public DocumentService(DocumentRepository documents, EmployeRepository employes,
                           ContratRepository contrats, OnboardingTacheRepository taches,
                           UtilisateurRepository utilisateurs, AuditService audit,
                           @Value("${app.upload.dir:uploads}") String uploadDir) {
        this.documents = documents;
        this.employes = employes;
        this.contrats = contrats;
        this.taches = taches;
        this.utilisateurs = utilisateurs;
        this.audit = audit;
        this.uploadDir = uploadDir;
    }

    @Transactional
    public DocumentModel enregistrer(MultipartFile fichier, DocumentForm form, EmployeModel auteur) {
        verifierFichier(fichier);
        if (form.getTitre() == null || form.getTitre().isBlank()) {
            throw new Bloc4Exception("Le titre du document est obligatoire.");
        }
        if (form.getTypeDocument() == null || form.getTypeDocument().isBlank()) {
            throw new Bloc4Exception("Le type de document est obligatoire.");
        }
        if (!TYPES.contains(form.getTypeDocument())) {
            throw new Bloc4Exception("Type de document invalide.");
        }
        if (!CONFIDENTIALITES.contains(form.getNiveauConfidentialite() == null
                ? "INTERNE" : form.getNiveauConfidentialite())) {
            throw new Bloc4Exception("Niveau de confidentialité invalide.");
        }

        EmployeModel employe = null;
        ContratModel contrat = null;
        OnboardingTacheModel tache = null;
        if (form.getIdContrat() != null) {
            contrat = contrats.findById(form.getIdContrat())
                    .orElseThrow(() -> new Bloc4Exception("Contrat introuvable."));
            employe = contrat.getEmploye();
        }
        if (form.getIdOnboardingTache() != null) {
            tache = taches.findById(form.getIdOnboardingTache())
                    .orElseThrow(() -> new Bloc4Exception("Tâche d'onboarding introuvable."));
            if (employe == null) {
                employe = tache.getOnboarding().getEmploye();
            }
        }
        if (form.getIdEmploye() != null) {
            employe = employes.findById(form.getIdEmploye())
                    .orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        }

        String extension = extension(fichier.getOriginalFilename());
        String nomStockage = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String sousDossier = LocalDateTime.now().format(DOSSIER);
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path dossier = base.resolve(sousDossier).normalize();
        try {
            Files.createDirectories(dossier);
            try (InputStream in = fichier.getInputStream()) {
                Files.copy(in, dossier.resolve(nomStockage), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new Bloc4Exception("Impossible d'enregistrer le fichier sur le disque.");
        }

        DocumentModel d = new DocumentModel();
        d.setTypeDocument(form.getTypeDocument());
        d.setTitre(form.getTitre().trim());
        d.setNomFichier(fichier.getOriginalFilename() == null ? nomStockage : fichier.getOriginalFilename());
        d.setCheminStockage(sousDossier + "/" + nomStockage);
        d.setTypeMime(typeMime(fichier, extension));
        d.setTailleOctets(fichier.getSize());
        d.setVersion(1);
        d.setStatutValidation("RECU");
        d.setNiveauConfidentialite(form.getNiveauConfidentialite() == null
                ? "INTERNE" : form.getNiveauConfidentialite());
        d.setEmploye(employe);
        d.setContrat(contrat);
        d.setOnboardingTache(tache);
        d.setDateDepot(LocalDateTime.now());
        d.setDeposePar(auteur);
        d.setCommentaire(form.getCommentaire());
        d = documents.save(d);
        audit.tracer("DOCUMENT", d.getId(), "DEPOT", null, d.getTitre(), null, auteur);
        return d;
    }

    @Transactional(readOnly = true)
    public DocumentResume detail(Integer id) {
        return resume(documents.findById(id)
                .orElseThrow(() -> new Bloc4Exception("Document introuvable.")));
    }

    @Transactional(readOnly = true)
    public Page<DocumentResume> lister(String type, String statut, int page, int taille) {
        int p = Math.max(page, 0);
        int t = Math.min(Math.max(taille, 1), 50);
        PageRequest pageable = PageRequest.of(p, t, Sort.by("dateDepot").descending());
        if (type != null && !type.isBlank()) {
            return documents.findByTypeDocument(type, pageable).map(this::resume);
        }
        if (statut != null && !statut.isBlank()) {
            return documents.findByStatutValidation(statut, pageable).map(this::resume);
        }
        return documents.findAll(pageable).map(this::resume);
    }

    @Transactional(readOnly = true)
    public List<DocumentResume> listerParContrat(Integer idContrat) {
        return documents.findByIdContratOrderByVersionDesc(idContrat).stream().map(this::resume).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResume> listerParTache(Integer idTache) {
        return documents.findByIdOnboardingTacheOrderByDateDepotDesc(idTache).stream().map(this::resume).toList();
    }

    @Transactional(readOnly = true)
    public List<DocumentResume> listerParEmploye(Integer idEmploye) {
        return documents.findByIdEmployeOrderByDateDepotDesc(idEmploye).stream().map(this::resume).toList();
    }

    @Transactional(readOnly = true)
    public Resource charger(Integer id, EmployeModel lecteur) {
        DocumentModel d = documents.findById(id)
                .orElseThrow(() -> new Bloc4Exception("Document introuvable."));
        String role = utilisateurs.findByIdEmploye(lecteur.getId())
                .map(UtilisateurModel::getRole).orElse("EMPLOYE");
        boolean habilite = "RH".equals(role) || "DG".equals(role);
        if (("SANTE".equals(d.getTypeDocument()) || "SECRET".equals(d.getNiveauConfidentialite()))
                && !habilite) {
            throw new Bloc4Exception("Vous n'êtes pas habilité à consulter ce document.");
        }
        if (!habilite) {
            boolean proprietaire = d.getEmploye() != null && d.getEmploye().getId().equals(lecteur.getId());
            boolean responsable = "RESPONSABLE".equals(role) && d.getEmploye() != null
                    && lecteur.getDepartement() != null && d.getEmploye().getDepartement() != null
                    && lecteur.getDepartement().getId().equals(d.getEmploye().getDepartement().getId());
            if (!proprietaire && !responsable) {
                throw new Bloc4Exception("Vous n'êtes pas autorisé à télécharger ce document.");
            }
        }
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path fichier = base.resolve(d.getCheminStockage()).normalize();
        if (!fichier.startsWith(base)) {
            throw new Bloc4Exception("Chemin de document invalide.");
        }
        if (!Files.exists(fichier)) {
            throw new Bloc4Exception("Le fichier demandé est introuvable sur le disque.");
        }
        return new FileSystemResource(fichier);
    }

    @Transactional
    public void verifier(Integer id, EmployeModel auteur) {
        DocumentModel d = documents.findById(id)
                .orElseThrow(() -> new Bloc4Exception("Document introuvable."));
        if (!"RECU".equals(d.getStatutValidation())) {
            throw new Bloc4Exception("Seul un document reçu peut être vérifié.");
        }
        d.setStatutValidation("VERIFIE");
        d.setDateVerification(LocalDateTime.now());
        d.setVerifiePar(auteur);
        documents.save(d);
        audit.tracer("DOCUMENT", id, "VERIFICATION", "RECU", "VERIFIE", null, auteur);
        if (d.getDeposePar() != null) {
            // notification informative, sans bloquer l'action
        }
    }

    @Transactional
    public void rejeter(Integer id, String motif, EmployeModel auteur) {
        if (motif == null || motif.isBlank()) {
            throw new Bloc4Exception("Le motif de rejet est obligatoire.");
        }
        DocumentModel d = documents.findById(id)
                .orElseThrow(() -> new Bloc4Exception("Document introuvable."));
        if (!"RECU".equals(d.getStatutValidation())) {
            throw new Bloc4Exception("Seul un document reçu peut être rejeté.");
        }
        d.setStatutValidation("REJETE");
        d.setMotifRejet(motif);
        d.setDateVerification(LocalDateTime.now());
        d.setVerifiePar(auteur);
        documents.save(d);
        audit.tracer("DOCUMENT", id, "REJET", "RECU", "REJETE", motif, auteur);
    }

    private void verifierFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new Bloc4Exception("Le fichier est obligatoire.");
        }
        if (fichier.getSize() > TAILLE_MAX) {
            throw new Bloc4Exception("Le fichier dépasse la taille maximale de 10 Mo.");
        }
        String extension = extension(fichier.getOriginalFilename());
        if (!EXTENSIONS.contains(extension)) {
            throw new Bloc4Exception("Format de fichier non autorisé. Formats acceptés : pdf, docx, doc, png, jpg, jpeg.");
        }
        String mime = fichier.getContentType();
        if (mime != null && !mime.isBlank()
                && !(mime.startsWith("application/") || mime.startsWith("image/"))) {
            throw new Bloc4Exception("Le type réel du fichier n'est pas autorisé.");
        }
    }

    private static String extension(String nom) {
        if (nom == null || !nom.contains(".")) {
            throw new Bloc4Exception("Le fichier doit posséder une extension.");
        }
        return nom.substring(nom.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private static String typeMime(MultipartFile fichier, String extension) {
        if (fichier.getContentType() != null && !fichier.getContentType().isBlank()) {
            return fichier.getContentType();
        }
        return switch (extension) {
            case "pdf" -> "application/pdf";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            default -> "application/octet-stream";
        };
    }

    private DocumentResume resume(DocumentModel d) {
        return new DocumentResume(d.getId(), d.getTitre(), d.getTypeDocument(), d.getTypeLibelle(),
                d.getEmploye() == null ? null : d.getEmploye().getNomComplet(),
                d.getContrat() == null ? null : d.getContrat().getReference(),
                d.getNomFichier(), Formats.taille(d.getTailleOctets()), d.getVersion(),
                d.getNiveauConfidentialite(), d.getConfidentialiteLibelle(), d.getStatutValidation(),
                d.getStatutLibelle(), d.getStatutBadge(), d.getDateDepotFr(), d.getTypeMime(),
                d.getCommentaire(), d.getMotifRejet(),
                d.getEmploye() == null ? null : d.getEmploye().getId(),
                d.getContrat() == null ? null : d.getContrat().getId(),
                d.getOnboardingTache() == null ? null : d.getOnboardingTache().getId());
    }
}
