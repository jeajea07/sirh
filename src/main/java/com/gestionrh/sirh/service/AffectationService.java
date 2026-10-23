package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.AffectationModel;
import com.gestionrh.sirh.model.DepartementModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.PosteModel;
import com.gestionrh.sirh.repository.AffectationRepository;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.service.dto.AffectationForm;
import com.gestionrh.sirh.service.dto.AffectationResume;

@Service
public class AffectationService {

    private final AffectationRepository affectations;
    private final EmployeRepository employes;
    private final PosteRepository postes;
    private final DepartementRepository departements;
    private final ContratRepository contrats;
    private final AuditService audit;

    public AffectationService(AffectationRepository affectations, EmployeRepository employes,
                              PosteRepository postes, DepartementRepository departements,
                              ContratRepository contrats, AuditService audit) {
        this.affectations = affectations;
        this.employes = employes;
        this.postes = postes;
        this.departements = departements;
        this.contrats = contrats;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<AffectationResume> listerParEmploye(Integer idEmploye) {
        return affectations.findByIdEmployeOrderByDateDebutDesc(idEmploye).stream().map(this::resume).toList();
    }

    @Transactional(readOnly = true)
    public List<AffectationResume> lister() {
        return affectations.findAll().stream().map(this::resume).toList();
    }

    @Transactional(readOnly = true)
    public AffectationForm formulaireNouvelle(Integer idEmploye) {
        AffectationForm f = new AffectationForm();
        f.setIdEmploye(idEmploye);
        f.setDateDebut(LocalDate.now());
        if (idEmploye != null) {
            employes.findById(idEmploye).ifPresent(e -> {
                if (e.getPoste() != null) f.setIdPoste(e.getPoste().getId());
                if (e.getDepartement() != null) f.setIdDepartement(e.getDepartement().getId());
            });
        }
        return f;
    }

    @Transactional(readOnly = true)
    public AffectationResume detail(Integer id) {
        return resume(affectations.findById(id)
                .orElseThrow(() -> new Bloc4Exception("Affectation introuvable.")));
    }

    public List<String> verifier(AffectationForm f) {
        List<String> erreurs = new ArrayList<>();
        if (f.getIdEmploye() == null || !employes.findById(f.getIdEmploye()).isPresent()) {
            erreurs.add("Le salarié est obligatoire.");
        }
        if (f.getIdPoste() == null || !postes.findById(f.getIdPoste()).isPresent()) {
            erreurs.add("Le poste est obligatoire.");
        }
        if (f.getIdDepartement() == null || !departements.findById(f.getIdDepartement()).isPresent()) {
            erreurs.add("Le département est obligatoire.");
        }
        if (f.getDateDebut() == null) {
            erreurs.add("La date de début est obligatoire.");
        }
        if (f.getDateFin() != null && f.getDateDebut() != null
                && !f.getDateFin().isAfter(f.getDateDebut())) {
            erreurs.add("La date de fin doit être postérieure à la date de début.");
        }
        if (f.getIdEmploye() != null && f.getDateDebut() != null) {
            LocalDate fin = f.getDateFin() == null ? LocalDate.of(9999, 12, 31) : f.getDateFin();
            if (affectations.countChevauchements(f.getIdEmploye(), f.getId(), f.getDateDebut(), fin) > 0) {
                erreurs.add("Une affectation active existe déjà sur cette période.");
            }
        }
        return erreurs;
    }

    @Transactional
    public AffectationModel enregistrer(AffectationForm f, EmployeModel auteur) {
        List<String> erreurs = verifier(f);
        if (!erreurs.isEmpty()) {
            throw new Bloc4Exception(String.join(" ", erreurs));
        }
        EmployeModel employe = employes.findById(f.getIdEmploye())
                .orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        PosteModel poste = postes.findById(f.getIdPoste())
                .orElseThrow(() -> new Bloc4Exception("Poste introuvable."));
        DepartementModel departement = departements.findById(f.getIdDepartement())
                .orElseThrow(() -> new Bloc4Exception("Département introuvable."));

        affectations.findFirstByIdEmployeAndStatutOrderByDateDebutDesc(employe.getId(), "ACTIVE")
                .ifPresent(ancienne -> {
                    if (ancienne.getDateDebut() != null && !f.getDateDebut().isAfter(ancienne.getDateDebut())) {
                        throw new Bloc4Exception("La nouvelle affectation doit commencer après l'affectation active.");
                    }
                    ancienne.setStatut("TERMINEE");
                    ancienne.setDateFin(f.getDateDebut().minusDays(1));
                    ancienne.setDateMaj(LocalDateTime.now());
                    affectations.save(ancienne);
                });

        AffectationModel a = new AffectationModel();
        a.setEmploye(employe);
        a.setPoste(poste);
        a.setDepartement(departement);
        a.setContrat(f.getIdContrat() == null ? null
                : contrats.findById(f.getIdContrat())
                        .orElseThrow(() -> new Bloc4Exception("Contrat introuvable.")));
        if (f.getIdResponsable() != null) {
            a.setResponsable(employes.findById(f.getIdResponsable()).orElse(null));
        }
        a.setSite(f.getSite());
        a.setDateDebut(f.getDateDebut());
        a.setDateFin(f.getDateFin());
        a.setMotif(f.getMotif());
        a.setStatut("ACTIVE");
        a.setDateCreation(LocalDateTime.now());
        a.setDateMaj(LocalDateTime.now());
        a.setCreePar(auteur);
        a = affectations.save(a);

        employe.setPoste(poste);
        employe.setDepartement(departement);
        employes.save(employe);
        audit.tracer("AFFECTATION", a.getId(), "CREATION", null,
                poste.getDesignation() + " / " + departement.getDesignation(), f.getMotif(), auteur);
        return a;
    }

    @Transactional
    public void cloturer(Integer id, LocalDate dateFin, EmployeModel auteur) {
        AffectationModel a = affectations.findById(id)
                .orElseThrow(() -> new Bloc4Exception("Affectation introuvable."));
        if (!a.isActive()) {
            throw new Bloc4Exception("Cette affectation est déjà terminée.");
        }
        if (dateFin == null || (a.getDateDebut() != null && dateFin.isBefore(a.getDateDebut()))) {
            throw new Bloc4Exception("La date de fin est invalide.");
        }
        a.setStatut("TERMINEE");
        a.setDateFin(dateFin);
        a.setDateMaj(LocalDateTime.now());
        affectations.save(a);
        audit.tracer("AFFECTATION", id, "CLOTURE", "ACTIVE", "TERMINEE",
                "Date de fin : " + dateFin, auteur);
    }

    @Transactional(readOnly = true)
    public AffectationModel affectationActive(Integer idEmploye) {
        return affectations.findFirstByIdEmployeAndStatutOrderByDateDebutDesc(idEmploye, "ACTIVE")
                .orElse(null);
    }

    private AffectationResume resume(AffectationModel a) {
        return new AffectationResume(a.getId(), a.getEmploye().getId(), a.getEmploye().getNomComplet(),
                a.getPoste() == null ? "—" : a.getPoste().getDesignation(),
                a.getDepartement() == null ? "—" : a.getDepartement().getDesignation(),
                a.getResponsable() == null ? "—" : a.getResponsable().getNomComplet(),
                a.getSite(), a.getMotif(), a.getDateDebut(), a.getDateFin(),
                a.getStatut(), a.getStatutLibelle(), a.getStatutBadge());
    }
}
