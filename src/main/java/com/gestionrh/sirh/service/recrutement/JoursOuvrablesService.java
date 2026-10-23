package com.gestionrh.sirh.service.recrutement;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Calcul indépendant du calendrier des jours ouvrables, prévu pour les tests unitaires. */
@Component
public class JoursOuvrablesService {

    private final Set<LocalDate> joursFeries;
    private final int delaiEtude;

    public JoursOuvrablesService(
            @Value("${app.recrutement.jours-feries:}") String joursFeries,
            @Value("${app.recrutement.delai-etude-jours:5}") int delaiEtude) {
        this.joursFeries = joursFeries == null || joursFeries.isBlank() ? Set.of()
                : Arrays.stream(joursFeries.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                        .map(LocalDate::parse).collect(Collectors.toUnmodifiableSet());
        this.delaiEtude = Math.max(0, delaiEtude);
    }

    public int joursOuvrablesEcoules(LocalDateTime reception, LocalDate dateReference) {
        if (reception == null || dateReference == null || !dateReference.isAfter(reception.toLocalDate())) return 0;
        int total = 0;
        for (LocalDate jour = reception.toLocalDate().plusDays(1); !jour.isAfter(dateReference); jour = jour.plusDays(1)) {
            if (estOuvrable(jour)) total++;
        }
        return total;
    }

    public boolean estEnRetard(LocalDateTime reception, LocalDate dateReference) {
        return joursOuvrablesEcoules(reception, dateReference) > delaiEtude;
    }

    public int joursDeRetard(LocalDateTime reception, LocalDate dateReference) {
        return Math.max(0, joursOuvrablesEcoules(reception, dateReference) - delaiEtude);
    }

    private boolean estOuvrable(LocalDate jour) {
        return jour.getDayOfWeek() != DayOfWeek.SATURDAY && jour.getDayOfWeek() != DayOfWeek.SUNDAY
                && !joursFeries.contains(jour);
    }
}
