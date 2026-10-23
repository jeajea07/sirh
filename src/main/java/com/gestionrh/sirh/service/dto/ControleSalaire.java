package com.gestionrh.sirh.service.dto;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ControleSalaire {

    public static final String CONFORME = "CONFORME";
    public static final String DEPASSE = "DEPASSE";
    public static final String SANS_GRILLE = "SANS_GRILLE";
    public static final String NON_SAISI = "NON_SAISI";

    private final String statut;
    private final BigDecimal salaire;
    private final BigDecimal plafond;

    public ControleSalaire(String statut, BigDecimal salaire, BigDecimal plafond) {
        this.statut = statut;
        this.salaire = salaire;
        this.plafond = plafond;
    }

    public static String formater(BigDecimal montant) {
        if (montant == null) {
            return "-";
        }
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        symboles.setDecimalSeparator(',');
        return new DecimalFormat("#,##0.##", symboles).format(montant) + " Ar";
    }

    public String getStatut() { return statut; }

    public boolean isDepasse() { return DEPASSE.equals(statut); }

    public BigDecimal getPlafond() { return plafond; }

    public String getPlafondFr() { return formater(plafond); }

    public String getPlafondBrut() { return plafond == null ? "" : plafond.toPlainString(); }

    public String getSalaireFr() { return formater(salaire); }

    public String getEcartFr() {
        if (salaire == null || plafond == null) {
            return "-";
        }
        return formater(salaire.subtract(plafond));
    }

    public String getLibelle() {
        if (DEPASSE.equals(statut)) {
            return "Au-dessus de la grille";
        }
        if (CONFORME.equals(statut)) {
            return "Conforme à la grille";
        }
        if (SANS_GRILLE.equals(statut)) {
            return "Pas de grille";
        }
        return "Non saisi";
    }

    public String getBadge() {
        if (DEPASSE.equals(statut)) {
            return "badge-bad";
        }
        if (CONFORME.equals(statut)) {
            return "badge-ok";
        }
        return "badge-warn";
    }

    public String getMessage() {
        if (DEPASSE.equals(statut)) {
            return "Le salaire proposé (" + getSalaireFr() + ") dépasse la grille salariale ("
                    + getPlafondFr() + ") de " + getEcartFr() + ".";
        }
        if (CONFORME.equals(statut)) {
            return "Le salaire proposé (" + getSalaireFr() + ") respecte la grille salariale ("
                    + getPlafondFr() + " maximum).";
        }
        if (SANS_GRILLE.equals(statut)) {
            return "Aucune ligne de la grille salariale ne correspond à ce poste : la vérification automatique est impossible.";
        }
        return "Aucun salaire n'est renseigné.";
    }
}
