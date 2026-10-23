package com.gestionrh.sirh.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/** Formats d'affichage communs au Bloc 4 (français, montants en ariary). */
public final class Formats {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Locale MG = Locale.FRANCE;

    private Formats() {
    }

    public static String date(LocalDate valeur) {
        return valeur == null ? "—" : valeur.format(DATE);
    }

    public static String dateHeure(LocalDateTime valeur) {
        return valeur == null ? "—" : valeur.format(DATE_HEURE);
    }

    public static String montant(BigDecimal valeur) {
        if (valeur == null) {
            return "—";
        }
        NumberFormat format = NumberFormat.getNumberInstance(MG);
        format.setMaximumFractionDigits(0);
        format.setMinimumFractionDigits(0);
        format.setRoundingMode(RoundingMode.HALF_UP);
        return format.format(valeur) + " Ar";
    }

    public static String taille(Long octets) {
        if (octets == null || octets <= 0) {
            return "—";
        }
        if (octets < 1024) {
            return octets + " o";
        }
        double ko = octets / 1024.0;
        if (ko < 1024) {
            return String.format(MG, "%.0f Ko", ko);
        }
        return String.format(MG, "%.1f Mo", ko / 1024.0);
    }

    public static long joursRestants(LocalDate fin) {
        return fin == null ? -1 : ChronoUnit.DAYS.between(LocalDate.now(), fin);
    }
}
