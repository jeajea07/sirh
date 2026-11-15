package com.gestionrh.sirh.event.recrutement;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gestionrh.sirh.service.Bloc2BService;
import com.gestionrh.sirh.service.recrutement.NotificationRecrutementService;

/**
 * Bloc 2 — étape 4 : « À la validation du dossier, l'application crée automatiquement l'accès du candidat
 * (identifiant et mot de passe provisoire). Il les reçoit dans l'e-mail de convocation. »
 */
@Component
public class DossierValideEventListener {

    private static final Logger log = LoggerFactory.getLogger(DossierValideEventListener.class);
    /** Seuil du test fixé avant le test (note minimale générale, sur 3). */
    private static final BigDecimal SEUIL_PAR_DEFAUT = BigDecimal.valueOf(2);

    private final Bloc2BService bloc2BService;
    private final NotificationRecrutementService notifications;

    public DossierValideEventListener(Bloc2BService bloc2BService, NotificationRecrutementService notifications) {
        this.bloc2BService = bloc2BService;
        this.notifications = notifications;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // Transaction propre : après commit, la transaction de la décision RH est terminée.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onDossierValide(DossierValideEvent event) {
        if (event == null || event.idCandidature() == null) {
            return;
        }
        Bloc2BService.AccesCandidat acces = bloc2BService.creerAccesCandidat(event.idCandidature(), SEUIL_PAR_DEFAUT);
        if (acces == null || event.idCandidat() == null) {
            return;
        }
        try {
            notifications.notifierCandidat(event.idCandidat(), "CONVOCATION_TEST",
                    "Convocation au test technique",
                    "Bonjour, votre dossier " + event.numero() + " est retenu. Vous êtes convoqué(e) au test technique,"
                            + " à passer dans nos locaux en une seule séance. Connectez-vous sur l'espace candidat avec"
                            + " l'identifiant " + acces.identifiant() + " et le mot de passe provisoire "
                            + acces.motDePasseTemporaire() + ".",
                    event.idOffre(), event.idCandidature());
        } catch (RuntimeException e) {
            log.warn("Convocation au test non créée pour la candidature {} : {}", event.numero(), e.getMessage());
        }
    }
}
