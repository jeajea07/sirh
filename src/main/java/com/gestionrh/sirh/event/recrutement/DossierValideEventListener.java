package com.gestionrh.sirh.event.recrutement;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.gestionrh.sirh.service.Bloc2BService;

@Component
public class DossierValideEventListener {

    private static final BigDecimal SEUIL_PAR_DEFAUT = BigDecimal.valueOf(2);

    private final Bloc2BService bloc2BService;

    public DossierValideEventListener(Bloc2BService bloc2BService) {
        this.bloc2BService = bloc2BService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDossierValide(DossierValideEvent event) {
        if (event == null || event.idCandidature() == null) {
            return;
        }
        bloc2BService.creerAccesCandidat(event.idCandidature(), SEUIL_PAR_DEFAUT);
    }
}
