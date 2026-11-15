package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.gestionrh.sirh.model.ModeleHoraireModel;
import com.gestionrh.sirh.model.RegimeTravailModel;
import com.gestionrh.sirh.model.TypeCongeModel;

/** Règles des Blocs 6 et 7 qui ne dépendent pas de la base (readme/Bloc6.md, readme/Bloc7.md). */
class Bloc67TempsCongesTest {

    private static final LocalDate LUNDI = LocalDate.of(2026, 11, 23);

    private CongeService conges(LocalDate ferie) {
        PrevisionService previsions = mock(PrevisionService.class);
        when(previsions.estFerie(any(LocalDate.class))).thenAnswer(i -> i.getArgument(0).equals(ferie));
        return new CongeService(null, null, null, null, null, null, null, null, null, previsions, null, null, null, null);
    }

    private static TypeCongeModel type(String mode) {
        TypeCongeModel t = new TypeCongeModel();
        t.setModeDecompte(mode);
        return t;
    }

    @Test
    void unCongeCalendaireCompteTousLesJours() {
        // Lundi → dimanche : 7 jours calendaires, férié compris (RG-DC03).
        BigDecimal n = conges(LUNDI.plusDays(2)).calculerJours(type("CALENDAIRE"), LUNDI, "MATIN", LUNDI.plusDays(6), "APRES_MIDI");
        assertThat(n).isEqualByComparingTo("7");
    }

    @Test
    void unCongeOuvreExclutWeekEndEtFeries() {
        // RG-DC04 : lundi → dimanche en jours ouvrés, mercredi férié → 4 jours.
        BigDecimal n = conges(LUNDI.plusDays(2)).calculerJours(type("OUVRE"), LUNDI, "MATIN", LUNDI.plusDays(6), "APRES_MIDI");
        assertThat(n).isEqualByComparingTo("4");
    }

    @Test
    void lesDemiJourneesSontDecomptees() {
        // Lundi après-midi → mardi midi : 1 jour.
        BigDecimal n = conges(null).calculerJours(type("CALENDAIRE"), LUNDI, "APRES_MIDI", LUNDI.plusDays(1), "MATIN");
        assertThat(n).isEqualByComparingTo("1");
        // Une seule demi-journée.
        assertThat(conges(null).calculerJours(type("CALENDAIRE"), LUNDI, "MATIN", LUNDI, "MATIN")).isEqualByComparingTo("0.5");
    }

    @Test
    void leChevauchementDePeriodesEstCompteEnJours() {
        assertThat(SoldeService.chevauchement(LUNDI, LUNDI.plusDays(4), LUNDI.plusDays(3), LUNDI.plusDays(10))).isEqualTo(2);
        assertThat(SoldeService.chevauchement(LUNDI, LUNDI, LUNDI.plusDays(1), LUNDI.plusDays(2))).isZero();
    }

    @Test
    void unHoraireDeNuitChevaucheMinuit() {
        // RG-RT05 : 22 h → 6 h avec 30 min de pause = 7 h 30.
        ModeleHoraireModel m = new ModeleHoraireModel();
        m.setHeureDebut(LocalTime.of(22, 0));
        m.setHeureFin(LocalTime.of(6, 0));
        m.setDureePauseMinutes(30);
        m.setEstNuit(true);
        assertThat(m.getDureeMinutes()).isEqualTo(450);
    }

    @Test
    void laDureeJournaliereDeReferenceVientDuRegime() {
        // 40 h sur 5 jours = 8 h : au-delà, une case du planning porte des heures sup. planifiées.
        RegimeTravailModel r = new RegimeTravailModel();
        r.setDureeHebdoHeures(new BigDecimal("40"));
        r.setJoursTravailles("1,2,3,4,5");
        assertThat(r.getMinutesJournalieres()).isEqualTo(480);
        assertThat(r.travaille(6)).isFalse();
        assertThat(r.travaille(1)).isTrue();
    }
}
