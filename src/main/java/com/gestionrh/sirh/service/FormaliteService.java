package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.DeclarationSocialeModel;
import com.gestionrh.sirh.model.DocumentModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.OrganismeModel;
import com.gestionrh.sirh.repository.DeclarationSocialeRepository;
import com.gestionrh.sirh.repository.DocumentRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.OrganismeRepository;
import com.gestionrh.sirh.service.dto.DeclarationForm;
import com.gestionrh.sirh.service.dto.DeclarationResume;
import com.gestionrh.sirh.util.Formats;

@Service
public class FormaliteService {

    private final DeclarationSocialeRepository declarations;
    private final EmployeRepository employes;
    private final OrganismeRepository organismes;
    private final DocumentRepository documents;
    private final AuditService audit;
    private final NotificationService notifications;

    public FormaliteService(DeclarationSocialeRepository declarations, EmployeRepository employes,
                            OrganismeRepository organismes, DocumentRepository documents,
                            AuditService audit, NotificationService notifications) {
        this.declarations = declarations;
        this.employes = employes;
        this.organismes = organismes;
        this.documents = documents;
        this.audit = audit;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public Page<DeclarationResume> lister(String statut, int page, int taille) {
        int p = Math.max(page, 0);
        int t = Math.min(Math.max(taille, 1), 50);
        PageRequest pageable = PageRequest.of(p, t, Sort.by("dateCreation").descending());
        if (statut != null && !statut.isBlank()) {
            return declarations.findByStatut(statut, pageable).map(this::resume);
        }
        return declarations.findAll(pageable).map(this::resume);
    }

    @Transactional(readOnly = true)
    public List<DeclarationResume> listerParEmploye(Integer idEmploye) {
        return declarations.findByIdEmployeOrderByDateCreationDesc(idEmploye).stream().map(this::resume).toList();
    }

    @Transactional(readOnly = true)
    public DeclarationForm formulaireNouveau(Integer idEmploye) {
        DeclarationForm f = new DeclarationForm();
        f.setIdEmploye(idEmploye);
        f.setDatePreparation(LocalDate.now());
        return f;
    }

    @Transactional(readOnly = true)
    public DeclarationForm formulaireDe(Integer id) {
        DeclarationSocialeModel d = charger(id);
        DeclarationForm f = new DeclarationForm();
        f.setId(d.getId());
        f.setIdEmploye(d.getEmploye().getId());
        f.setIdOrganisme(d.getOrganisme().getId());
        f.setTypeDeclaration(d.getTypeDeclaration());
        f.setReferenceExterne(d.getReferenceExterne());
        f.setCommentaire(d.getCommentaire());
        f.setDatePreparation(d.getDatePreparation());
        f.setDateEnvoi(d.getDateEnvoi());
        f.setIdDocumentJustificatif(d.getDocumentJustificatif() == null ? null
                : d.getDocumentJustificatif().getId());
        return f;
    }

    @Transactional(readOnly = true)
    public DeclarationResume detail(Integer id) {
        return resume(charger(id));
    }

    public List<String> verifier(DeclarationForm f) {
        List<String> erreurs = new ArrayList<>();
        if (f.getIdEmploye() == null || !employes.findById(f.getIdEmploye()).isPresent()) {
            erreurs.add("Le salarié est obligatoire.");
        }
        if (f.getIdOrganisme() == null || !organismes.findById(f.getIdOrganisme()).isPresent()) {
            erreurs.add("L'organisme est obligatoire.");
        }
        if (f.getTypeDeclaration() == null || f.getTypeDeclaration().isBlank()) {
            erreurs.add("Le type de déclaration est obligatoire.");
        }
        return erreurs;
    }

    @Transactional
    public DeclarationSocialeModel enregistrer(DeclarationForm f, EmployeModel auteur) {
        List<String> erreurs = verifier(f);
        if (!erreurs.isEmpty()) {
            throw new Bloc4Exception(String.join(" ", erreurs));
        }
        DeclarationSocialeModel d;
        if (f.getId() == null) {
            d = new DeclarationSocialeModel();
            d.setStatut("A_PREPARER");
            d.setDateCreation(LocalDateTime.now());
            d.setCreePar(auteur);
        } else {
            d = charger(f.getId());
            if (!"A_PREPARER".equals(d.getStatut())) {
                throw new Bloc4Exception("Cette déclaration ne peut plus être modifiée.");
            }
        }
        d.setEmploye(employes.findById(f.getIdEmploye()).orElseThrow());
        d.setOrganisme(organismes.findById(f.getIdOrganisme()).orElseThrow());
        d.setTypeDeclaration(f.getTypeDeclaration().trim());
        d.setDatePreparation(f.getDatePreparation());
        d.setReferenceExterne(f.getReferenceExterne());
        d.setCommentaire(f.getCommentaire());
        if (f.getIdDocumentJustificatif() != null) {
            d.setDocumentJustificatif(documents.findById(f.getIdDocumentJustificatif())
                    .orElseThrow(() -> new Bloc4Exception("Justificatif introuvable.")));
        }
        d.setDateMaj(LocalDateTime.now());
        d = declarations.save(d);
        audit.tracer("DECLARATION", d.getId(), f.getId() == null ? "CREATION" : "MODIFICATION",
                null, d.getTypeDeclaration(), null, auteur);
        return d;
    }

    @Transactional
    public void marquerPreparee(Integer id, EmployeModel auteur) {
        DeclarationSocialeModel d = charger(id);
        if (!"A_PREPARER".equals(d.getStatut())) {
            throw new Bloc4Exception("Seule une déclaration à préparer peut être marquée préparée.");
        }
        d.setStatut("PREPAREE");
        d.setDatePreparation(d.getDatePreparation() == null ? LocalDate.now() : d.getDatePreparation());
        d.setDateMaj(LocalDateTime.now());
        declarations.save(d);
        audit.tracer("DECLARATION", id, "PREPARATION", "A_PREPARER", "PREPAREE", null, auteur);
    }

    @Transactional
    public void marquerTransmise(Integer id, LocalDate dateEnvoi, String referenceExterne,
                                 EmployeModel auteur) {
        if (dateEnvoi == null || referenceExterne == null || referenceExterne.isBlank()) {
            throw new Bloc4Exception("La date d'envoi et la référence externe sont obligatoires.");
        }
        DeclarationSocialeModel d = charger(id);
        if (!"PREPAREE".equals(d.getStatut())) {
            throw new Bloc4Exception("La déclaration doit d'abord être préparée.");
        }
        d.setStatut("TRANSMISE");
        d.setDateEnvoi(dateEnvoi);
        d.setReferenceExterne(referenceExterne.trim());
        d.setDateMaj(LocalDateTime.now());
        declarations.save(d);
        notifierRh("DECLARATION", id, "Déclaration " + d.getTypeDeclaration() + " transmise.");
        audit.tracer("DECLARATION", id, "TRANSMISSION", "PREPAREE", "TRANSMISE",
                referenceExterne, auteur);
    }

    @Transactional
    public void marquerConfirmee(Integer id, Integer idDocumentJustificatif, EmployeModel auteur) {
        if (idDocumentJustificatif == null) {
            throw new Bloc4Exception("Un justificatif est obligatoire pour confirmer la déclaration.");
        }
        DeclarationSocialeModel d = charger(id);
        if (!"TRANSMISE".equals(d.getStatut())) {
            throw new Bloc4Exception("La déclaration doit d'abord être transmise.");
        }
        DocumentModel doc = documents.findById(idDocumentJustificatif)
                .orElseThrow(() -> new Bloc4Exception("Justificatif introuvable."));
        if (!doc.getEmploye().getId().equals(d.getEmploye().getId())) {
            throw new Bloc4Exception("Le justificatif doit appartenir au même salarié.");
        }
        if (!"SOCIAL".equals(doc.getTypeDocument()) && !"SANTE".equals(doc.getTypeDocument())) {
            throw new Bloc4Exception("Le justificatif doit être de type SOCIAL ou SANTE.");
        }
        d.setDocumentJustificatif(doc);
        d.setStatut("CONFIRMEE");
        d.setDateMaj(LocalDateTime.now());
        declarations.save(d);
        notifierRh("DECLARATION", id, "Déclaration " + d.getTypeDeclaration() + " confirmée.");
        audit.tracer("DECLARATION", id, "CONFIRMATION", "TRANSMISE", "CONFIRMEE", null, auteur);
    }

    @Transactional
    public void rejeter(Integer id, String motif, EmployeModel auteur) {
        if (motif == null || motif.isBlank()) {
            throw new Bloc4Exception("Le motif de rejet est obligatoire.");
        }
        DeclarationSocialeModel d = charger(id);
        String avant = d.getStatut();
        d.setStatut("REJETEE");
        d.setCommentaire((d.getCommentaire() == null ? "" : d.getCommentaire() + " ") + "Rejet : " + motif);
        d.setDateMaj(LocalDateTime.now());
        declarations.save(d);
        audit.tracer("DECLARATION", id, "REJET", avant, "REJETEE", motif, auteur);
    }

    private DeclarationSocialeModel charger(Integer id) {
        return declarations.findById(id)
                .orElseThrow(() -> new Bloc4Exception("Déclaration introuvable."));
    }

    private DeclarationResume resume(DeclarationSocialeModel d) {
        return new DeclarationResume(d.getId(), d.getEmploye().getId(), d.getEmploye().getNomComplet(),
                d.getOrganisme().getNom(), d.getTypeDeclaration(),
                Formats.date(d.getDatePreparation()), Formats.date(d.getDateEnvoi()),
                d.getReferenceExterne(), d.getStatut(), d.getStatutLibelle(), d.getStatutBadge(),
                d.getCommentaire(), d.getDocumentJustificatif() == null ? null
                        : d.getDocumentJustificatif().getId());
    }

    private void notifierRh(String type, Integer id, String message) {
        employes.validateursParRole("RH").forEach(rh -> notifications.notifier(rh.getId(), type, id, message));
    }
}
