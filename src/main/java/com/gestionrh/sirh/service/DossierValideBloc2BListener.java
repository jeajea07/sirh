package com.gestionrh.sirh.service;

import com.gestionrh.sirh.event.recrutement.DossierValideEvent;
import com.gestionrh.sirh.service.recrutement.NotificationRecrutementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Crée l'accès au test après validation RH du dossier, une fois la transaction BLOC 2A validée. */
@Component
public class DossierValideBloc2BListener {

    private static final Logger log = LoggerFactory.getLogger(DossierValideBloc2BListener.class);

    private final Bloc2BService bloc2B;
    private final NotificationRecrutementService notifications;

    public DossierValideBloc2BListener(Bloc2BService bloc2B,
            NotificationRecrutementService notifications) {
        this.bloc2B = bloc2B;
        this.notifications = notifications;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void dossierValide(DossierValideEvent event) {
        try {
            Bloc2BService.AccesCandidat acces = bloc2B.creerAccesCandidat(event.idCandidature().longValue());
            if (acces.motDePasseTemporaire() == null) return;
            String message = "Votre dossier " + event.numero()
                    + " est validé. Vous pouvez accéder au test technique depuis la page de connexion.\n"
                    + "Identifiant : " + acces.identifiant() + "\n"
                    + "Mot de passe temporaire : " + acces.motDePasseTemporaire()
                    + "\nChangez-le après votre première connexion.";
            notifications.notifierAccesTestCandidat(event.idCandidat(), "Accès au test technique", message,
                    event.idOffre(), event.idCandidature());
        } catch (RuntimeException e) {
            log.error("Échec de préparation de l'accès au test pour candidature id={}", event.idCandidature(), e);
        }
    }
}
