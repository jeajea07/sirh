package com.gestionrh.sirh.dto;

import java.math.BigDecimal;

/** Résultat de la comparaison entre le coût annuel d'une demande et le budget de son département. */
public class ControleBudget {

    public static final String CONFORME = "CONFORME";
    public static final String DEPASSE = "DEPASSE";
    public static final String ALLEGE = "ALLEGE";
    public static final String SANS_BUDGET = "SANS_BUDGET";
    public static final String NON_SAISI = "NON_SAISI";

    private final String statut;
    private final int annee;
    private final BigDecimal alloue;
    private final BigDecimal engage;
    private final BigDecimal cout;

    public ControleBudget(String statut, int annee, BigDecimal alloue, BigDecimal engage, BigDecimal cout) {
        this.statut = statut;
        this.annee = annee;
        this.alloue = alloue;
        this.engage = engage;
        this.cout = cout;
    }

    public String getStatut() { return statut; }
    public int getAnnee() { return annee; }
    public boolean isDepasse() { return DEPASSE.equals(statut); }
    public boolean isAvecBudget() { return alloue != null; }

    public BigDecimal getDisponible() {
        return alloue == null ? null : alloue.subtract(engage);
    }

    public BigDecimal getManque() {
        if (cout == null || alloue == null) {
            return null;
        }
        BigDecimal manque = cout.subtract(getDisponible());
        return manque.signum() > 0 ? manque : BigDecimal.ZERO;
    }

    public String getAlloueFr() { return ControleSalaire.formater(alloue); }
    public String getEngageFr() { return ControleSalaire.formater(engage); }
    public String getDisponibleFr() { return ControleSalaire.formater(getDisponible()); }
    public String getCoutFr() { return ControleSalaire.formater(cout); }
    public String getManqueFr() { return ControleSalaire.formater(getManque()); }

    public String getLibelle() {
        if (DEPASSE.equals(statut)) { return "Budget insuffisant"; }
        if (CONFORME.equals(statut)) { return "Budget suffisant"; }
        if (ALLEGE.equals(statut)) { return "Remplacement"; }
        if (SANS_BUDGET.equals(statut)) { return "Pas de budget"; }
        return "Non saisi";
    }

    public String getBadge() {
        if (DEPASSE.equals(statut)) { return "badge-bad"; }
        if (CONFORME.equals(statut) || ALLEGE.equals(statut)) { return "badge-ok"; }
        return "badge-warn";
    }

    public String getAlerte() {
        if (DEPASSE.equals(statut)) { return "alert-bad"; }
        if (CONFORME.equals(statut) || ALLEGE.equals(statut)) { return "alert-ok"; }
        return "alert-warn";
    }

    public String getMessage() {
        if (ALLEGE.equals(statut)) {
            return "Demande de remplacement : le poste est déjà budgété, son coût n'est pas compté dans le budget "
                    + annee + ".";
        }
        if (SANS_BUDGET.equals(statut)) {
            return "Aucun budget n'est défini pour ce département en " + annee + " : la vérification automatique est impossible.";
        }
        if (DEPASSE.equals(statut)) {
            return "Le coût annuel de la demande (" + getCoutFr() + ") dépasse le budget disponible du département en "
                    + annee + " (" + getDisponibleFr() + ") : il manque " + getManqueFr() + ".";
        }
        if (CONFORME.equals(statut)) {
            return "Le coût annuel de la demande (" + getCoutFr() + ") rentre dans le budget disponible du département en "
                    + annee + " (" + getDisponibleFr() + ").";
        }
        return "Aucun salaire n'est renseigné.";
    }
}
