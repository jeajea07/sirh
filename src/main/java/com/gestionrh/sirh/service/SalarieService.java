package com.gestionrh.sirh.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.service.dto.ContratResume;
import com.gestionrh.sirh.service.dto.DocumentResume;
import com.gestionrh.sirh.service.dto.SalarieDetail;
import com.gestionrh.sirh.service.dto.SalarieForm;
import com.gestionrh.sirh.service.dto.SalarieResume;

@Service
public class SalarieService {

    private final EmployeRepository employes;
    private final DepartementRepository departements;
    private final PosteRepository postes;
    private final ContratService contrats;
    private final AffectationService affectations;
    private final OnboardingService onboardings;
    private final DocumentService documents;
    private final FormaliteService formalites;
    private final AuditService audit;
    private final SalaireService salaires;
    private final CarriereService carriere;
    private final DossierDocumentService pieces;

    public SalarieService(EmployeRepository employes, DepartementRepository departements,
                          PosteRepository postes, ContratService contrats,
                          AffectationService affectations, OnboardingService onboardings,
                          DocumentService documents, FormaliteService formalites, AuditService audit,
                          SalaireService salaires, CarriereService carriere, DossierDocumentService pieces) {
        this.salaires = salaires;
        this.carriere = carriere;
        this.pieces = pieces;
        this.employes = employes;
        this.departements = departements;
        this.postes = postes;
        this.contrats = contrats;
        this.affectations = affectations;
        this.onboardings = onboardings;
        this.documents = documents;
        this.formalites = formalites;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Page<SalarieResume> lister(String recherche, Integer idDepartement, int page, int taille) {
        int p = Math.max(page, 0);
        int t = Math.min(Math.max(taille, 1), 50);
        return employes.rechercher(recherche == null ? null : recherche.trim(), idDepartement,
                        PageRequest.of(p, t, Sort.by("nom").ascending().and(Sort.by("prenom"))))
                .map(this::resume);
    }

    @Transactional(readOnly = true)
    public SalarieDetail detail(Integer idEmploye, String role) {
        EmployeModel e = employes.findById(idEmploye)
                .orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        SalarieDetail d = new SalarieDetail();
        d.setSalarie(resume(e));
        d.setCin(e.getCin());
        d.setDateNaissance(e.getDateNaissance());
        d.setLieuNaissance(e.getLieuNaissance());
        d.setAdresse(e.getAdresse());
        d.setSituationFamiliale(e.getSituationFamiliale());
        d.setNombreEnfants(e.getNombreEnfants());
        d.setNumeroCnaps(e.getNumeroCnaps());
        d.setNumeroOstie(e.getNumeroOstie());
        d.setRib(e.getRib());
        d.setContrats(contrats.listerParEmploye(idEmploye));
        d.setAffectations(affectations.listerParEmploye(idEmploye));
        d.setOnboardings(onboardings.listerParEmploye(idEmploye));
        // RG-AC01 / RG-AC03 : le RH, la DG et le salarié lui-même ("SELF") voient les données sensibles.
        boolean habilite = "RH".equals(role) || "DG".equals(role) || "SELF".equals(role);
        List<DocumentResume> docs = new ArrayList<>(documents.listerParEmploye(idEmploye));
        if (!habilite) {
            docs.removeIf(DocumentResume::isSensible);
        }
        d.setDocuments(docs);
        d.setFormalites(formalites.listerParEmploye(idEmploye));
        d.setHistorique(new ArrayList<>());

        // --- Bloc 5 : situation actuelle calculée, jamais saisie (§4.2, RG-SP03) ---
        d.setSexe(e.getSexe());
        d.setNationalite(e.getNationalite());
        d.setContactUrgenceNom(e.getContactUrgenceNom());
        d.setContactUrgenceTelephone(e.getContactUrgenceTelephone());
        d.setDateNotificationSortie(e.getDateNotificationSortie());
        d.setDateSortie(e.getDateSortie());
        d.setMotifSortie(e.getMotifSortie());
        d.setContratEnVigueur(d.getContrats().stream()
                .filter(c -> "EN_VIGUEUR".equals(c.getStatutWorkflow())).findFirst().orElse(null));
        d.setDonneesSensiblesVisibles(habilite);
        if (habilite) {
            d.setSalaireEnVigueur(salaires.enVigueurAu(idEmploye, java.time.LocalDate.now()).orElse(null));
            d.setHistoriqueSalaires(salaires.historique(idEmploye));
        } else {
            d.setRib(null);
        }
        d.setCarriere(carriere.chronologie(idEmploye));
        d.setPieces(pieces.piecesObligatoires(idEmploye));
        d.setAnciennete(anciennete(e));
        d.setAlertes(alertes(e, d));
        return d;
    }

    /** Ancienneté depuis la date d'embauche, arrêtée à la date de sortie le cas échéant (RG-SP05). */
    private static String anciennete(EmployeModel e) {
        if (e.getDateEmbauche() == null) {
            return "—";
        }
        java.time.LocalDate fin = e.getDateSortie() != null && !e.getDateSortie().isAfter(java.time.LocalDate.now())
                ? e.getDateSortie() : java.time.LocalDate.now();
        if (fin.isBefore(e.getDateEmbauche())) {
            return "Pas encore en poste";
        }
        java.time.Period p = java.time.Period.between(e.getDateEmbauche(), fin);
        String ans = p.getYears() == 0 ? "" : p.getYears() + (p.getYears() > 1 ? " ans " : " an ");
        return (ans + p.getMonths() + " mois").trim();
    }

    private static List<String> alertes(EmployeModel e, SalarieDetail d) {
        List<String> alertes = new ArrayList<>();
        if ("EN_PREAVIS".equals(e.getStatutAdministratif()) && e.getDateSortie() != null) {
            alertes.add("Sortie prévue le " + com.gestionrh.sirh.util.Formats.date(e.getDateSortie()) + ".");
        }
        if ("SUSPENDU".equals(e.getStatutAdministratif())) {
            alertes.add("Contrat suspendu.");
        }
        boolean enPoste = !"SORTI".equals(e.getStatutAdministratif()) && !"EN_INTEGRATION".equals(e.getStatutAdministratif());
        if (enPoste && d.getContratEnVigueur() == null) {
            alertes.add("Aucun contrat en vigueur (RG-SP01).");
        }
        if (enPoste && d.getAffectations().stream().noneMatch(a -> "ACTIVE".equals(a.getStatut()))) {
            alertes.add("Aucune affectation active (RG-SP02).");
        }
        ContratResume c = d.getContratEnVigueur();
        if (c != null && c.getDateFin() != null) {
            long jours = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), c.getDateFin());
            if (jours >= 0 && jours <= 30) {
                alertes.add("Le contrat " + c.getReference() + " se termine dans " + jours + " jour(s) : décision à prendre (RG-CT06).");
            } else if (jours < 0) {
                alertes.add("Le contrat " + c.getReference() + " est échu sans décision (RG-CT07).");
            }
        }
        long bloquantes = d.getNombrePiecesBloquantes();
        if (!"SORTI".equals(e.getStatutAdministratif()) && bloquantes > 0) {
            alertes.add("Dossier incomplet : " + bloquantes + " pièce(s) obligatoire(s) manquante(s), rejetée(s) ou expirée(s).");
        }
        return alertes;
    }

    @Transactional(readOnly = true)
    public SalarieForm formulaireDe(Integer idEmploye) {
        EmployeModel e = employes.findById(idEmploye)
                .orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        SalarieForm f = new SalarieForm();
        f.setId(e.getId());
        f.setNom(e.getNom());
        f.setPrenom(e.getPrenom());
        f.setEmail(e.getEmail());
        f.setTelephone(e.getTelephone());
        f.setCin(e.getCin());
        f.setLieuNaissance(e.getLieuNaissance());
        f.setAdresse(e.getAdresse());
        f.setSituationFamiliale(e.getSituationFamiliale());
        f.setNumeroCnaps(e.getNumeroCnaps());
        f.setNumeroOstie(e.getNumeroOstie());
        f.setRib(e.getRib());
        f.setDateNaissance(e.getDateNaissance());
        f.setDateEmbauche(e.getDateEmbauche());
        f.setNombreEnfants(e.getNombreEnfants());
        f.setSexe(e.getSexe());
        f.setNationalite(e.getNationalite());
        f.setContactUrgenceNom(e.getContactUrgenceNom());
        f.setContactUrgenceTelephone(e.getContactUrgenceTelephone());
        return f;
    }

    public List<String> verifier(SalarieForm f) {
        List<String> erreurs = new ArrayList<>();
        if (f.getNom() == null || f.getNom().isBlank()) erreurs.add("Le nom est obligatoire.");
        if (f.getPrenom() == null || f.getPrenom().isBlank()) erreurs.add("Le prénom est obligatoire.");
        if (f.getEmail() == null || f.getEmail().isBlank()) {
            erreurs.add("L'email est obligatoire.");
        } else if (!f.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            erreurs.add("L'email n'est pas valide.");
        } else {
            employes.findByEmailIgnoreCase(f.getEmail().trim()).ifPresent(e -> {
                if (f.getId() == null || !e.getId().equals(f.getId())) {
                    erreurs.add("Cet email est déjà utilisé par un autre salarié.");
                }
            });
        }
        if (f.getCin() != null && !f.getCin().isBlank()) {
            employes.findByCin(f.getCin().trim()).ifPresent(e -> {
                if (f.getId() == null || !e.getId().equals(f.getId())) {
                    erreurs.add("Ce CIN est déjà utilisé par un autre salarié.");
                }
            });
        }
        if (f.getNombreEnfants() != null && f.getNombreEnfants() < 0) {
            erreurs.add("Le nombre d'enfants ne peut pas être négatif (RG-IP08).");
        }
        if (f.getSexe() != null && !f.getSexe().isBlank() && !List.of("M", "F").contains(f.getSexe())) {
            erreurs.add("Le sexe est invalide.");
        }
        if (f.getNumeroCnaps() != null && !f.getNumeroCnaps().isBlank()) {
            employes.findByNumeroCnaps(f.getNumeroCnaps().trim()).ifPresent(e -> {
                if (f.getId() == null || !e.getId().equals(f.getId())) {
                    erreurs.add("Ce N° CNaPS est déjà utilisé par un autre salarié (RG-IP02).");
                }
            });
        }
        if (f.getDateNaissance() != null && f.getDateNaissance().isAfter(java.time.LocalDate.now())) {
            erreurs.add("La date de naissance ne peut pas être dans le futur.");
        }
        return erreurs;
    }

    @Transactional
    public EmployeModel enregistrer(SalarieForm f, EmployeModel auteur) {
        List<String> erreurs = verifier(f);
        if (!erreurs.isEmpty()) {
            throw new Bloc4Exception(String.join(" ", erreurs));
        }
        EmployeModel e = employes.findById(f.getId())
                .orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        if ("SORTI".equals(e.getStatutAdministratif())) {
            throw new Bloc4Exception("Le dossier d'un salarié sorti est en lecture seule (RG-SO03).");
        }
        String avant = empreinte(e);
        e.setNom(f.getNom().trim());
        e.setPrenom(f.getPrenom().trim());
        e.setEmail(f.getEmail().trim());
        e.setTelephone(f.getTelephone());
        e.setCin(f.getCin());
        e.setLieuNaissance(f.getLieuNaissance());
        e.setAdresse(f.getAdresse());
        e.setSituationFamiliale(f.getSituationFamiliale());
        e.setNumeroCnaps(f.getNumeroCnaps());
        e.setNumeroOstie(f.getNumeroOstie());
        e.setRib(f.getRib());
        e.setDateNaissance(f.getDateNaissance());
        e.setDateEmbauche(f.getDateEmbauche());
        e.setNombreEnfants(f.getNombreEnfants());
        e.setSexe(vide(f.getSexe()));
        e.setNationalite(vide(f.getNationalite()));
        e.setContactUrgenceNom(vide(f.getContactUrgenceNom()));
        e.setContactUrgenceTelephone(vide(f.getContactUrgenceTelephone()));
        e.setNumeroCnaps(vide(f.getNumeroCnaps()));
        e.setCin(vide(f.getCin()));
        e = employes.save(e);
        // RG-IP05 : ancienne et nouvelle valeur de chaque champ modifié.
        String apres = empreinte(e);
        audit.tracer("SALARIE", e.getId(), "MODIFICATION", ecart(avant, apres, true), ecart(avant, apres, false),
                "Fiche salarié", auteur);
        return e;
    }

    private static String vide(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }

    /** Champs suivis par l'audit ; le RIB n'est jamais écrit en clair dans le journal. */
    private static java.util.Map<String, String> empreinteMap(EmployeModel e) {
        java.util.Map<String, String> m = new java.util.LinkedHashMap<>();
        m.put("Nom", e.getNom());
        m.put("Prénom", e.getPrenom());
        m.put("Email", e.getEmail());
        m.put("Téléphone", e.getTelephone());
        m.put("CIN", e.getCin());
        m.put("Naissance", String.valueOf(e.getDateNaissance()));
        m.put("Situation familiale", e.getSituationFamiliale());
        m.put("Enfants", String.valueOf(e.getNombreEnfants()));
        m.put("Adresse", e.getAdresse());
        m.put("CNaPS", e.getNumeroCnaps());
        m.put("OSTIE", e.getNumeroOstie());
        m.put("RIB", e.getRib() == null ? null : "****" + e.getRib().substring(Math.max(0, e.getRib().length() - 4)));
        m.put("Sexe", e.getSexe());
        m.put("Nationalité", e.getNationalite());
        m.put("Contact d'urgence", e.getContactUrgenceNom() + " " + e.getContactUrgenceTelephone());
        return m;
    }

    private static String empreinte(EmployeModel e) {
        StringBuilder sb = new StringBuilder();
        empreinteMap(e).forEach((k, v) -> sb.append(k).append('=').append(v).append('\n'));
        return sb.toString();
    }

    private static String ecart(String avant, String apres, boolean ancien) {
        String[] a = avant.split("\n");
        String[] b = apres.split("\n");
        List<String> champs = new ArrayList<>();
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            if (!a[i].equals(b[i])) {
                champs.add(ancien ? a[i] : b[i]);
            }
        }
        return champs.isEmpty() ? null : String.join(" ; ", champs);
    }

    private SalarieResume resume(EmployeModel e) {
        SalarieResume r = new SalarieResume(e.getId(), e.getMatricule(), e.getNom(), e.getPrenom(), e.getEmail(),
                e.getTelephone(), e.getPoste() == null ? "—" : e.getPoste().getDesignation(),
                e.getDepartement() == null ? "—" : e.getDepartement().getDesignation(),
                e.getDateEmbauche(), e.isActif(), e.getInitiales());
        r.setStatutAdministratif(e.getStatutAdministratif());
        return r;
    }
}
