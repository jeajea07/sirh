package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.event.recrutement.CandidatureNonRetenueEvent;
import com.gestionrh.sirh.event.recrutement.DossierValideEvent;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.model.recrutement.HistoriqueCandidature;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.HistoriqueCandidatureRepository;
import java.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DecisionCandidatureService {

    private final CandidatureRepository candidatures;
    private final HistoriqueCandidatureRepository historiques;
    private final ApplicationEventPublisher events;

    public DecisionCandidatureService(CandidatureRepository candidatures,
            HistoriqueCandidatureRepository historiques, ApplicationEventPublisher events) {
        this.candidatures = candidatures;
        this.historiques = historiques;
        this.events = events;
    }

    @Transactional
    public Candidature validerAutomatique(Integer id, Integer idValidateur) {
        Candidature c = chargerPourDecision(id);
        if (c.getClassementAuto() == Classement.A_REGARDER) {
            throw new RegleMetierException("Le classement « À regarder » doit être tranché avant validation.");
        }
        return enregistrer(c, c.getClassementAuto(), null, idValidateur);
    }

    @Transactional
    public Candidature decider(Integer id, Classement classementFinal, String commentaire, Integer idValidateur) {
        Candidature c = chargerPourDecision(id);
        if (classementFinal == null || classementFinal == Classement.A_REGARDER) {
            throw new RegleMetierException("Choisissez CONFORME ou NON_CONFORME comme décision finale.");
        }
        if (classementFinal != c.getClassementAuto() && (commentaire == null || commentaire.isBlank())) {
            throw new RegleMetierException("Un commentaire est obligatoire lorsque le classement automatique est modifié.");
        }
        return enregistrer(c, classementFinal, commentaire, idValidateur);
    }

    private Candidature chargerPourDecision(Integer id) {
        Candidature c = candidatures.verrouillerParId(id)
                .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));
        if (c.getStatut() != CandidatureStatut.A_ETUDIER || c.getDateValidation() != null) {
            throw new RegleMetierException("Cette candidature n'est plus en attente d'étude.");
        }
        if (c.getClassementAuto() == null) throw new RegleMetierException("Le classement automatique est absent.");
        return c;
    }

    private Candidature enregistrer(Candidature c, Classement classementFinal, String commentaire, Integer idValidateur) {
        CandidatureStatut ancienStatut = c.getStatut();
        Classement ancienClassement = c.getClassementAuto();
        CandidatureStatut nouveauStatut = classementFinal == Classement.CONFORME
                ? CandidatureStatut.DOSSIER_VALIDE : CandidatureStatut.NON_RETENUE;
        c.setClassementFinal(classementFinal);
        c.setIdValidateur(idValidateur);
        c.setDateValidation(LocalDateTime.now());
        c.setStatut(nouveauStatut);
        candidatures.save(c);

        HistoriqueCandidature h = new HistoriqueCandidature();
        h.setCandidature(c);
        h.setAction("VALIDATION_RH");
        h.setAncienStatut(ancienStatut);
        h.setNouveauStatut(nouveauStatut);
        h.setAncienClassement(ancienClassement);
        h.setNouveauClassement(classementFinal);
        h.setIdAuteur(idValidateur);
        h.setCommentaire(commentaire == null || commentaire.isBlank() ? "Classement automatique confirmé." : commentaire.trim());
        historiques.save(h);

        if (classementFinal == Classement.CONFORME) {
            String numero = c.getNumero();
            Integer candidatId = c.getCandidat().getIdCandidat();
            Integer offreId = c.getOffre() == null ? null : c.getOffre().getIdOffre();
            events.publishEvent(new DossierValideEvent(c.getIdCandidature(), candidatId, offreId, numero));
        } else {
            events.publishEvent(new CandidatureNonRetenueEvent(c.getIdCandidature()));
        }
        return c;
    }
}
