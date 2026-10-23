package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.event.recrutement.CandidatureNonRetenueEvent;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationNonRetenueListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationNonRetenueListener.class);
    private final CandidatureRepository candidatures;
    private final NotificationRecrutementService notifications;

    public NotificationNonRetenueListener(CandidatureRepository candidatures, NotificationRecrutementService notifications) {
        this.candidatures = candidatures;
        this.notifications = notifications;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifier(CandidatureNonRetenueEvent event) {
        Candidature c = candidatures.findWithCandidatAndOffreByIdCandidature(event.idCandidature()).orElse(null);
        if (c == null) return;
        try {
            notifications.notifierCandidat(c.getCandidat().getIdCandidat(), "CANDIDATURE_NON_RETENUE",
                    "Réponse à votre candidature", ModeleNotificationRecrutement.reponseNonConforme(c.getNumero()),
                    c.getOffre() == null ? null : c.getOffre().getIdOffre(), c.getIdCandidature());
        } catch (RuntimeException e) {
            log.warn("Impossible de créer la réponse non retenue pour la candidature {}", c.getNumero(), e);
        }
    }
}
