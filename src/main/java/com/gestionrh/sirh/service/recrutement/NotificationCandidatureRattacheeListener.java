package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.event.recrutement.CandidatureRattacheeEvent;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationCandidatureRattacheeListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationCandidatureRattacheeListener.class);
    private final CandidatureRepository candidatures;
    private final DestinatairesRhResolver destinatairesRh;
    private final NotificationRecrutementService notifications;

    public NotificationCandidatureRattacheeListener(CandidatureRepository candidatures,
            DestinatairesRhResolver destinatairesRh, NotificationRecrutementService notifications) {
        this.candidatures = candidatures;
        this.destinatairesRh = destinatairesRh;
        this.notifications = notifications;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifier(CandidatureRattacheeEvent event) {
        Candidature candidature = candidatures.findWithCandidatAndOffreByIdCandidature(event.idCandidature()).orElse(null);
        if (candidature == null || candidature.getOffre() == null) return;
        for (Integer idRh : destinatairesRh.trouverDestinataires()) {
            try {
                notifications.notifierEmploye(idRh, "CANDIDATURE_RATTACHEE", "Candidature rattachée à une offre",
                        "La candidature " + candidature.getNumero() + " est rattachée à " + candidature.getOffre().getReference() + ".",
                        candidature.getOffre().getIdOffre(), candidature.getIdCandidature());
            } catch (RuntimeException e) {
                log.warn("Impossible de notifier le rattachement de la candidature {}", candidature.getNumero(), e);
            }
        }
    }
}
