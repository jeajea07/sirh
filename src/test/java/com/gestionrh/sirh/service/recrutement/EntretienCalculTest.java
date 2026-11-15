package com.gestionrh.sirh.service.recrutement;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.gestionrh.sirh.model.EntretienModel;
import com.gestionrh.sirh.model.EntretienNoteModel;

/** Bloc 2 — notes d'entretien : moyenne pondérée, seuil général et questions éliminatoires. */
class EntretienCalculTest {

    @Test
    void moyennePondereeParImportanceEtAdmisSiSeuilAtteint() {
        EntretienModel e = entretien("3");
        e.ajouterNote(note(1, false, null, "5"));   // importance 1
        e.ajouterNote(note(3, false, null, "3"));   // importance 3 : (5*1 + 3*3) / 4 = 3.5
        EntretienService.calculer(e);
        assertThat(e.getNoteGenerale()).isEqualByComparingTo("3.50");
        assertThat(e.getResultatPropose()).isEqualTo(EntretienModel.ADMIS);
    }

    @Test
    void questionEliminatoireEchoueeProposeNonAdmisMemeAvecBonneMoyenne() {
        EntretienModel e = entretien("3");
        e.ajouterNote(note(1, true, "3", "2"));
        e.ajouterNote(note(3, false, null, "5"));
        EntretienService.calculer(e);
        assertThat(e.getNoteGenerale()).isGreaterThanOrEqualTo(new BigDecimal("3"));
        assertThat(e.getResultatPropose()).isEqualTo(EntretienModel.NON_ADMIS);
        assertThat(e.getMotifProposition()).contains("éliminatoire");
    }

    @Test
    void noteGeneraleSousLeSeuilProposeNonAdmis() {
        EntretienModel e = entretien("4");
        e.ajouterNote(note(1, false, null, "3"));
        EntretienService.calculer(e);
        assertThat(e.getResultatPropose()).isEqualTo(EntretienModel.NON_ADMIS);
        assertThat(e.getMotifProposition()).contains("seuil");
    }

    private EntretienModel entretien(String seuil) {
        EntretienModel e = new EntretienModel();
        e.setSeuilGeneral(new BigDecimal(seuil));
        return e;
    }

    private EntretienNoteModel note(int importance, boolean eliminatoire, String min, String valeur) {
        EntretienNoteModel n = new EntretienNoteModel();
        n.setQuestion("Q");
        n.setImportance((short) importance);
        n.setEliminatoire(eliminatoire);
        n.setNoteMinimale(min == null ? null : new BigDecimal(min));
        n.setNote(new BigDecimal(valeur));
        return n;
    }
}
