package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.dto.recrutement.CandidatureReserveResume;
import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.event.recrutement.CandidatureRattacheeEvent;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.model.recrutement.HistoriqueCandidature;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.HistoriqueCandidatureRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RattachementCandidatureService {

    private final CandidatureRepository candidatures;
    private final HistoriqueCandidatureRepository historiques;
    private final OffreService offres;
    private final ClassementCandidatureService classement;
    private final ApplicationEventPublisher events;

    public RattachementCandidatureService(CandidatureRepository candidatures,
            HistoriqueCandidatureRepository historiques, OffreService offres,
            ClassementCandidatureService classement, ApplicationEventPublisher events) {
        this.candidatures = candidatures;
        this.historiques = historiques;
        this.offres = offres;
        this.classement = classement;
        this.events = events;
    }

    public Page<CandidatureReserveResume> rechercherReserve(String q, Pageable pageable) {
        return candidatures.rechercherReserve(q == null ? "" : q.trim(), pageable).map(c ->
                new CandidatureReserveResume(c.getIdCandidature(), c.getNumero(), c.getCandidat().getNom(),
                        c.getCandidat().getPrenom(), c.getCandidat().getEmail(), c.getDateReception()));
    }

    @Transactional
    public Candidature rattacher(Integer idCandidature, Integer idOffre) {
        Candidature candidature = candidatures.findWithCandidatAndOffreByIdCandidature(idCandidature)
                .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));
        if (!Boolean.TRUE.equals(candidature.getSpontanee()) || candidature.getOffre() != null
                || candidature.getStatut() != CandidatureStatut.RECUE) {
            throw new RegleMetierException("Cette candidature n'est plus dans la réserve à rattacher.");
        }
        Offre offre = offres.trouverOuverte(idOffre);
        if (candidatures.existsByCandidat_IdCandidatAndOffre_IdOffreAndStatutNot(
                candidature.getCandidat().getIdCandidat(), offre.getIdOffre(), CandidatureStatut.RETIREE)) {
            throw new RegleMetierException("Ce candidat possède déjà une candidature active pour cette offre.");
        }
        CandidatureStatut ancien = candidature.getStatut();
        candidature.setOffre(offre);
        candidature.setDateRattachement(LocalDateTime.now());
        candidature.setStatut(CandidatureStatut.A_ETUDIER);
        candidature = candidatures.saveAndFlush(candidature);
        historiser(candidature, "RATTACHEMENT", ancien, CandidatureStatut.A_ETUDIER, null, null,
                "Candidature spontanée rattachée à l'offre " + offre.getReference());

        List<String> competences = candidature.getCompetences().stream().map(c -> c.getLibelle()).toList();
        List<com.gestionrh.sirh.model.recrutement.CritereOffre> criteres = new java.util.ArrayList<>(
                offres.criteres(offre.getIdOffre(), com.gestionrh.sirh.enums.recrutement.TypeCritere.OBLIGATOIRE));
        criteres.addAll(offres.criteres(offre.getIdOffre(), com.gestionrh.sirh.enums.recrutement.TypeCritere.SOUHAITE));
        ResultatClassement resultat = classement.classer(candidature, criteres, competences);
        candidature.setClassementAuto(resultat.classement());
        candidature.setMotifClassement(resultat.motif());
        candidatures.save(candidature);
        historiser(candidature, "CLASSEMENT_AUTO", CandidatureStatut.A_ETUDIER, CandidatureStatut.A_ETUDIER,
                null, resultat.classement(), resultat.motif());
        offres.recalculerNombreCandidatures(offre.getIdOffre());
        events.publishEvent(new CandidatureRattacheeEvent(candidature.getIdCandidature()));
        return candidature;
    }

    private void historiser(Candidature candidature, String action, CandidatureStatut ancien,
            CandidatureStatut nouveau, Classement ancienClassement, Classement nouveauClassement, String commentaire) {
        HistoriqueCandidature h = new HistoriqueCandidature();
        h.setCandidature(candidature);
        h.setAction(action);
        h.setAncienStatut(ancien);
        h.setNouveauStatut(nouveau);
        h.setAncienClassement(ancienClassement);
        h.setNouveauClassement(nouveauClassement);
        h.setCommentaire(commentaire);
        historiques.save(h);
    }
}
