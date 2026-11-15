package com.gestionrh.sirh.event.recrutement;

import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.gestionrh.sirh.service.Bloc2BService;
import com.gestionrh.sirh.service.recrutement.NotificationRecrutementService;

@ExtendWith(MockitoExtension.class)
class DossierValideEventListenerTest {

    @Mock
    private Bloc2BService bloc2BService;

    @Mock
    private NotificationRecrutementService notifications;

    @InjectMocks
    private DossierValideEventListener listener;

    @Test
    void shouldCreateCandidateAccessWhenDossierIsValidated() {
        DossierValideEvent event = new DossierValideEvent(42, 10, 99, "C-2026-0042");

        listener.onDossierValide(event);

        verify(bloc2BService).creerAccesCandidat(42, BigDecimal.valueOf(2));
    }
}
