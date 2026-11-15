package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.PointageModel;
import com.gestionrh.sirh.repository.PointageRepository;

/** Bloc 6 §7 — enregistrement des pointages (RG-PT). */
@Service
public class PointageService {

    /** RG-PT02 : deux pointages à moins de N minutes sont un doublon. */
    private static final int DOUBLON_MINUTES = 1;

    private final PointageRepository pointages;
    private final CalculTempsService calcul;

    public PointageService(PointageRepository pointages, CalculTempsService calcul) {
        this.pointages = pointages;
        this.calcul = calcul;
    }

    @Transactional(readOnly = true)
    public List<PointageModel> duJour(EmployeModel e, LocalDate jour) {
        return pointages.valides(e.getId(), jour.atStartOfDay(), jour.plusDays(1).atStartOfDay());
    }

    /** Pointage web : le sens est déduit du dernier pointage des 16 dernières heures. */
    @Transactional
    public PointageModel pointer(EmployeModel e) {
        String statut = e.getStatutAdministratif();
        if (!e.isActif() || "SORTI".equals(statut) || "SUSPENDU".equals(statut)) {
            throw new Bloc4Exception("Votre statut (" + EmployeModel.libelleStatut(statut) + ") ne permet pas de pointer (RG-PT05).");
        }
        LocalDateTime maintenant = LocalDateTime.now().withNano(0);
        List<PointageModel> recents = pointages.valides(e.getId(), maintenant.minusHours(16), maintenant.plusMinutes(1));
        PointageModel dernier = recents.isEmpty() ? null : recents.get(recents.size() - 1);
        if (dernier != null && ChronoUnit.MINUTES.between(dernier.getHorodatage(), maintenant) < DOUBLON_MINUTES) {
            throw new Bloc4Exception("Pointage déjà enregistré à " + dernier.getHeureFr() + " (doublon ignoré).");
        }
        String sens = dernier != null && "ENTREE".equals(dernier.getSens()) ? "SORTIE" : "ENTREE";
        return enregistrer(e, maintenant, sens, "WEB", null, null, null);
    }

    /** RG-PT03 / RG-PT04 : saisie manuelle par le RH ou le manager, motivée, jamais pour soi-même. */
    @Transactional
    public PointageModel saisirManuellement(EmployeModel e, LocalDateTime horodatage, String sens, String motif,
                                           Integer idCorrige, EmployeModel auteur) {
        if (auteur.getId().equals(e.getId())) {
            throw new Bloc4Exception("Vous ne pouvez pas saisir vos propres pointages : demandez une régularisation (RG-PT04).");
        }
        if (motif == null || motif.isBlank()) {
            throw new Bloc4Exception("Le motif d'un pointage manuel est obligatoire (RG-PT03).");
        }
        if (horodatage == null || !"ENTREE".equals(sens) && !"SORTIE".equals(sens)) {
            throw new Bloc4Exception("L'heure et le sens du pointage sont obligatoires.");
        }
        PointageModel p = enregistrer(e, horodatage.withNano(0), sens, "MANUEL", auteur, motif.trim(), idCorrige);
        calcul.recalculer(e, horodatage.toLocalDate().minusDays(1), horodatage.toLocalDate());
        return p;
    }

    /** RG-PT01 : un pointage erroné est annulé (jamais supprimé ni modifié). */
    @Transactional
    public void annuler(Integer idPointage, String motif, EmployeModel auteur) {
        PointageModel p = pointages.findById(idPointage).orElseThrow(() -> new Bloc4Exception("Pointage introuvable."));
        if (motif == null || motif.isBlank()) throw new Bloc4Exception("Le motif d'annulation est obligatoire.");
        if (calcul.periodeCloturee(p.getHorodatage().toLocalDate())) {
            throw new Bloc4Exception("La période est clôturée (RG-PT06).");
        }
        p.setStatut("ANNULE");
        p.setMotif((p.getMotif() == null ? "" : p.getMotif() + " — ") + "Annulé par " + auteur.getNomComplet() + " : " + motif);
        pointages.save(p);
        calcul.recalculer(p.getEmploye(), p.getHorodatage().toLocalDate().minusDays(1), p.getHorodatage().toLocalDate());
    }

    private PointageModel enregistrer(EmployeModel e, LocalDateTime h, String sens, String source,
                                      EmployeModel auteur, String motif, Integer idCorrige) {
        if (h.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new Bloc4Exception("Un pointage ne peut pas être dans le futur (RG-PT06).");
        }
        if (calcul.periodeCloturee(h.toLocalDate())) {
            throw new Bloc4Exception("La période de paie de ce jour est clôturée (RG-PT06).");
        }
        PointageModel p = new PointageModel();
        p.setEmploye(e);
        p.setHorodatage(h);
        p.setSens(sens);
        p.setSource(source);
        p.setSaisiPar(auteur);
        p.setMotif(motif);
        p.setIdPointageCorrige(idCorrige);
        return pointages.save(p);
    }
}
