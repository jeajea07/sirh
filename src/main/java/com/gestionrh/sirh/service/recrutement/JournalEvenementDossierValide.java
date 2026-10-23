package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.event.recrutement.DossierValideEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Journalise le point d'intégration tant que le bloc 2B n'a pas encore branché son listener. */
@Component
public class JournalEvenementDossierValide {

    private static final Logger log = LoggerFactory.getLogger(JournalEvenementDossierValide.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void journaliser(DossierValideEvent event) {
        log.info("DossierValideEvent transmis au point d'intégration BLOC 2B — candidature {}", event.numero());
    }
}
