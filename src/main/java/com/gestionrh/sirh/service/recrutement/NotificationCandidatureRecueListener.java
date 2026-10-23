package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.event.recrutement.CandidatureRecueEvent;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationCandidatureRecueListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationCandidatureRecueListener.class);
    private final CandidatureRepository candidatures;
    private final DestinatairesRhResolver destinatairesRh;
    private final NotificationRecrutementService notifications;

    public NotificationCandidatureRecueListener(CandidatureRepository candidatures,
            DestinatairesRhResolver destinatairesRh, NotificationRecrutementService notifications) {
        this.candidatures = candidatures;
        this.destinatairesRh = destinatairesRh;
        this.notifications = notifications;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifier(CandidatureRecueEvent event) {
        Candidature candidature = candidatures.findWithCandidatAndOffreByIdCandidature(event.idCandidature()).orElse(null);
        if (candidature == null) return;
        try {
            notifications.notifierCandidat(candidature.getCandidat().getIdCandidat(), "CANDIDATURE_RECUE",
                    "Votre dossier est en cours d'étude", ModeleNotificationRecrutement.accuseReception(candidature.getNumero()),
                    candidature.getOffre() == null ? null : candidature.getOffre().getIdOffre(), candidature.getIdCandidature());
        } catch (RuntimeException e) {
            log.warn("Impossible de créer l'accusé de réception pour la candidature {}", candidature.getNumero(), e);
        }
        for (Integer idRh : destinatairesRh.trouverDestinataires()) {
            try {
                notifications.notifierEmploye(idRh, "NOUVELLE_CANDIDATURE", "Nouvelle candidature reçue",
                        ModeleNotificationRecrutement.nouvelleCandidature(candidature.getNumero()),
                        candidature.getOffre() == null ? null : candidature.getOffre().getIdOffre(), candidature.getIdCandidature());
            } catch (RuntimeException e) {
                log.warn("Impossible de notifier le RH pour la candidature {}", candidature.getNumero(), e);
            }
        }
    }
}
