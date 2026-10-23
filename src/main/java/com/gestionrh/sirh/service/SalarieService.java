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

    public SalarieService(EmployeRepository employes, DepartementRepository departements,
                          PosteRepository postes, ContratService contrats,
                          AffectationService affectations, OnboardingService onboardings,
                          DocumentService documents, FormaliteService formalites, AuditService audit) {
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
        List<DocumentResume> docs = new ArrayList<>(documents.listerParEmploye(idEmploye));
        if (!"RH".equals(role) && !"DG".equals(role)) {
            docs.removeIf(DocumentResume::isSensible);
        }
        d.setDocuments(docs);
        d.setFormalites(formalites.listerParEmploye(idEmploye));
        d.setHistorique(new ArrayList<>());
        return d;
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
        e = employes.save(e);
        audit.tracer("SALARIE", e.getId(), "MODIFICATION", null, "Fiche salarié", null, auteur);
        return e;
    }

    private SalarieResume resume(EmployeModel e) {
        return new SalarieResume(e.getId(), e.getMatricule(), e.getNom(), e.getPrenom(), e.getEmail(),
                e.getTelephone(), e.getPoste() == null ? "—" : e.getPoste().getDesignation(),
                e.getDepartement() == null ? "—" : e.getDepartement().getDesignation(),
                e.getDateEmbauche(), e.isActif(), e.getInitiales());
    }
}
