package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.AbsenceModel;
import com.gestionrh.sirh.model.AnomaliePointageModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.AbsenceRepository;
import com.gestionrh.sirh.repository.AnomaliePointageRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.TypeCongeRepository;
import com.gestionrh.sirh.util.Formats;

/** Bloc 6 §9 — régularisation des anomalies de pointage (RG-AN). */
@Service
public class AnomalieService {

    private final AnomaliePointageRepository anomalies;
    private final AbsenceRepository absences;
    private final TypeCongeRepository types;
    private final EmployeRepository employes;
    private final ManagerService managers;
    private final PointageService pointages;
    private final CalculTempsService calcul;
    private final NotificationService notifications;
    private final TempsParametres parametres;

    public AnomalieService(AnomaliePointageRepository anomalies, AbsenceRepository absences, TypeCongeRepository types,
                           EmployeRepository employes, ManagerService managers, PointageService pointages,
                           CalculTempsService calcul, NotificationService notifications, TempsParametres parametres) {
        this.anomalies = anomalies;
        this.absences = absences;
        this.types = types;
        this.employes = employes;
        this.managers = managers;
        this.pointages = pointages;
        this.calcul = calcul;
        this.notifications = notifications;
        this.parametres = parametres;
    }

    @Transactional(readOnly = true)
    public List<AnomaliePointageModel> duSalarie(EmployeModel e) {
        return anomalies.findByEmploye_IdOrderByJourDesc(e.getId());
    }

    /** Anomalies à traiter par un manager (son équipe) ou par le RH (toutes, et les escaladées). */
    @Transactional(readOnly = true)
    public List<AnomaliePointageModel> aTraiterPar(EmployeModel valideur, boolean rh) {
        List<AnomaliePointageModel> toutes = anomalies.findByStatutInOrderByJourAsc(List.of("OUVERTE", "EN_COURS"));
        if (rh) return toutes;
        return toutes.stream().filter(a -> managers.estManagerDe(valideur, a.getEmploye())).toList();
    }

    /** RG-AN03 : le salarié justifie, en proposant éventuellement l'heure manquante. */
    @Transactional
    public void justifier(Integer id, EmployeModel salarie, String justification, LocalDateTime heure, String sens) {
        AnomaliePointageModel a = charger(id);
        if (!a.getEmploye().getId().equals(salarie.getId())) {
            throw new Bloc4Exception("Cette anomalie ne vous concerne pas.");
        }
        if (!a.isATraiter()) throw new Bloc4Exception("Cette anomalie est déjà traitée.");
        if (justification == null || justification.isBlank()) throw new Bloc4Exception("La justification est obligatoire.");
        if (heure != null && sens == null) throw new Bloc4Exception("Précisez s'il s'agit d'une entrée ou d'une sortie.");
        a.setJustification(justification.trim());
        a.setHeureProposee(heure);
        a.setSensPropose(heure == null ? null : sens);
        a.setDateJustification(LocalDateTime.now());
        a.setStatut("EN_COURS");
        anomalies.save(a);
        EmployeModel manager = managers.managerDe(salarie);
        if (manager != null) {
            notifications.notifier(manager.getId(), "ANOMALIE", id, salarie.getNomComplet() + " a justifié : "
                    + a.getTypeLibelle().toLowerCase() + " du " + a.getJourFr() + ".");
        }
    }

    /** RG-AN05 : régularisation acceptée → pointage correctif éventuel, puis recalcul de la journée. */
    @Transactional
    public void accepter(Integer id, String commentaire, EmployeModel valideur, boolean rh) {
        AnomaliePointageModel a = chargerPourDecision(id, valideur, rh);
        a.setStatut("RESOLUE");
        a.setTraitePar(valideur);
        a.setDateTraitement(LocalDateTime.now());
        a.setCommentaireTraitement(commentaire == null || commentaire.isBlank() ? "Justification acceptée." : commentaire.trim());
        anomalies.save(a);
        if (a.getHeureProposee() != null) {
            pointages.saisirManuellement(a.getEmploye(), a.getHeureProposee(), a.getSensPropose(),
                    "Régularisation de l'anomalie #" + id + " : " + a.getJustification(), null, valideur);
        } else {
            calcul.calculer(a.getEmploye(), a.getJour());
        }
        notifications.notifier(a.getEmploye().getId(), "ANOMALIE", id,
                "Votre justification (" + a.getTypeLibelle().toLowerCase() + " du " + a.getJourFr() + ") est acceptée.");
    }

    /** Refus : l'anomalie est confirmée (retard ou absence retenus) — RG-AN06. */
    @Transactional
    public void refuser(Integer id, String motif, EmployeModel valideur, boolean rh) {
        if (motif == null || motif.isBlank()) throw new Bloc4Exception("Le motif du refus est obligatoire.");
        AnomaliePointageModel a = chargerPourDecision(id, valideur, rh);
        confirmer(a, motif.trim(), valideur);
        notifications.notifier(a.getEmploye().getId(), "ANOMALIE", id,
                a.getTypeLibelle() + " du " + a.getJourFr() + " confirmée : " + motif);
    }

    /** RG-AN08 : classement sans suite, réservé au RH, motivé. */
    @Transactional
    public void classer(Integer id, String motif, EmployeModel rh) {
        if (motif == null || motif.isBlank()) throw new Bloc4Exception("Le motif du classement est obligatoire (RG-AN08).");
        AnomaliePointageModel a = charger(id);
        if (!a.isATraiter()) throw new Bloc4Exception("Cette anomalie est déjà traitée.");
        a.setStatut("CLASSEE");
        a.setTraitePar(rh);
        a.setDateTraitement(LocalDateTime.now());
        a.setCommentaireTraitement(motif.trim());
        anomalies.save(a);
        calcul.calculer(a.getEmploye(), a.getJour());
    }

    /** RG-AN04 : sans réponse dans le délai, l'anomalie est escaladée au RH. */
    @Transactional
    public int escalader() {
        List<AnomaliePointageModel> liste = anomalies.aEscalader(LocalDateTime.now().minusDays(parametres.delaiJustificationJours));
        for (AnomaliePointageModel a : liste) {
            a.setEscaladee(true);
            anomalies.save(a);
        }
        if (!liste.isEmpty()) {
            employes.validateursParRole("RH").forEach(rh -> notifications.notifier(rh.getId(), "ANOMALIE", null,
                    liste.size() + " anomalie(s) de pointage sans justification depuis " + parametres.delaiJustificationJours
                            + " jours : à traiter par le RH."));
        }
        return liste.size();
    }

    private void confirmer(AnomaliePointageModel a, String motif, EmployeModel valideur) {
        a.setStatut("CONFIRMEE");
        a.setTraitePar(valideur);
        a.setDateTraitement(LocalDateTime.now());
        a.setCommentaireTraitement(motif);
        anomalies.save(a);
        if ("ABSENCE_NON_JUSTIFIEE".equals(a.getTypeAnomalie())
                && absences.activesEntre(a.getEmploye().getId(), a.getJour(), a.getJour()).isEmpty()) {
            AbsenceModel abs = new AbsenceModel();
            abs.setEmploye(a.getEmploye());
            abs.setType(types.findByCode("ABSENCE_INJUSTIFIEE")
                    .orElseThrow(() -> new Bloc4Exception("Type ABSENCE_INJUSTIFIEE absent du paramétrage (bloc7.sql).")));
            abs.setDateDebut(a.getJour());
            abs.setDateFin(a.getJour());
            abs.setOrigine("CONSTATEE_POINTAGE");
            abs.setStatut("INJUSTIFIEE");
            abs.setIdAnomaliePointage(a.getId());
            abs.setCommentaire("Absence constatée au pointage le " + Formats.date(a.getJour()) + " : " + motif);
            abs.setSaisiPar(valideur);
            abs.setDecideur(valideur);
            abs.setDateDecision(LocalDateTime.now());
            absences.save(abs);
        }
        calcul.calculer(a.getEmploye(), a.getJour());
    }

    private AnomaliePointageModel chargerPourDecision(Integer id, EmployeModel valideur, boolean rh) {
        AnomaliePointageModel a = charger(id);
        if (!a.isATraiter()) throw new Bloc4Exception("Cette anomalie est déjà traitée.");
        if (a.getEmploye().getId().equals(valideur.getId())) {
            throw new Bloc4Exception("Vous ne pouvez pas traiter votre propre anomalie.");
        }
        if (!rh && !managers.estManagerDe(valideur, a.getEmploye())) {
            throw new Bloc4Exception("Seul le manager du salarié (ou le RH) peut traiter cette anomalie.");
        }
        if (calcul.periodeCloturee(a.getJour())) throw new Bloc4Exception("La période est clôturée (RG-VP02).");
        return a;
    }

    private AnomaliePointageModel charger(Integer id) {
        return anomalies.findById(id).orElseThrow(() -> new Bloc4Exception("Anomalie introuvable."));
    }

    public LocalDate aujourdhui() { return LocalDate.now(); }
}
