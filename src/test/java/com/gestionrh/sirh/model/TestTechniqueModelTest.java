package com.gestionrh.sirh.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.gestionrh.sirh.service.Bloc2BStatus;

class TestTechniqueModelTest {

    @Test
    void shouldTrackAbsenceAndManualCorrectionState() {
        TestTechniqueModel test = new TestTechniqueModel();

        assertEquals(Bloc2BStatus.TEST_PROGRAMME, test.getStatut());

        test.marquerAbsence("Maladie");
        assertTrue(test.isAbsent());
        assertEquals("Maladie", test.getMotifAbsence());
        assertEquals(Bloc2BStatus.TEST_ABSENT, test.getStatut());

        test.setCorrectionManuelle(true);
        assertTrue(test.isCorrectionManuelle());
        assertEquals(Bloc2BStatus.TEST_CORRIGE, test.getStatut());
    }

    @Test
    void shouldKeepManualCorrectionMetadataForEachAnswer() {
        ReponseTestTechniqueModel reponse = new ReponseTestTechniqueModel();
        reponse.setQuestionRef("Q-12");
        reponse.setOrdreQuestion(12);
        reponse.setTypeQuestion("LIBRE");

        reponse.corrigerManuellement(new BigDecimal("4.00"), true, "Très bonne réponse");

        assertEquals("Q-12", reponse.getQuestionRef());
        assertEquals(12, reponse.getOrdreQuestion());
        assertTrue(reponse.isCorrigee());
        assertTrue(reponse.isCorrectionManuelle());
        assertEquals("Très bonne réponse", reponse.getCommentaireCorrecteur());
    }

    @Test
    void shouldDetectExpiredSessionWithServerDeadline() {
        TestTechniqueModel test = new TestTechniqueModel();
        test.setDateDebut(LocalDateTime.now().minusMinutes(40));
        test.setDureeSecondes(1800);

        assertTrue(test.estExpiree());
    }
}
