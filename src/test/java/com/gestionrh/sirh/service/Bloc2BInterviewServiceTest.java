package com.gestionrh.sirh.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

class Bloc2BInterviewServiceTest {

    @Test
    void shouldAdvanceCandidateThroughAllInterviewStages() {
        CandidatureRepository candidatures = mock(CandidatureRepository.class);
        CandidatRepository candidats = mock(CandidatRepository.class);
        TestTechniqueRepository tests = mock(TestTechniqueRepository.class);
        ReponseTestTechniqueRepository reponses = mock(ReponseTestTechniqueRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

        Candidature candidature = new Candidature();
        candidature.setStatut(CandidatureStatut.CONFIRMATION_FINALE);
        when(candidatures.findById(13)).thenReturn(Optional.of(candidature));
        when(candidatures.save(any(Candidature.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bloc2BService service = new Bloc2BService(candidatures, candidats, tests, reponses, passwordEncoder);

        service.programmerEntretien(13, "RH");
        assertEquals(CandidatureStatut.ENTRETIEN_RH, candidature.getStatut());

        service.validerEntretien(13, "RH", true);
        assertEquals(CandidatureStatut.ENTRETIEN_TECHNICIEN, candidature.getStatut());

        service.validerEntretien(13, "TECHNICIEN", true);
        assertEquals(CandidatureStatut.ENTRETIEN_DG, candidature.getStatut());

        service.validerEntretien(13, "DG", true);
        assertEquals(CandidatureStatut.CONFIRMATION_FINALE, candidature.getStatut());

        service.confirmerFinale(13, true);
        assertEquals(CandidatureStatut.CONFIRMATION_FINALE, candidature.getStatut());

        candidature.setStatut(CandidatureStatut.ENTRETIEN_RH);
        service.noterEntretien(13, "RH", new BigDecimal("4.5"), true);
        assertEquals(CandidatureStatut.ENTRETIEN_TECHNICIEN, candidature.getStatut());
    }
}
