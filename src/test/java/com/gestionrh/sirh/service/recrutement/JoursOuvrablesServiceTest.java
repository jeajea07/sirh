package com.gestionrh.sirh.service.recrutement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JoursOuvrablesServiceTest {

    @Test
    void exclutWeekEndsEtJoursFeriesEtAppliqueLeDelai() {
        JoursOuvrablesService service = new JoursOuvrablesService("2026-10-22", 5);
        LocalDateTime reception = LocalDate.of(2026, 10, 16).atTime(9, 0); // vendredi
        assertEquals(3, service.joursOuvrablesEcoules(reception, LocalDate.of(2026, 10, 22)));
        assertFalse(service.estEnRetard(reception, LocalDate.of(2026, 10, 22)));
        assertTrue(service.estEnRetard(reception, LocalDate.of(2026, 10, 27)));
        assertEquals(1, service.joursDeRetard(reception, LocalDate.of(2026, 10, 27)));
    }
}
