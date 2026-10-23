package com.gestionrh.sirh.service;

import com.gestionrh.sirh.dto.ControleBudget;
import com.gestionrh.sirh.model.DemandeBesoinModel;

public class VerificationFinance {

    private final DemandeBesoinModel demande;
    private final String demandeur;
    private final String dateReceptionFr;
    private final ControleBudget controleBudget;
    private final boolean dureeRequise;

    public VerificationFinance(DemandeBesoinModel demande, String demandeur, String dateReceptionFr,
                               ControleBudget controleBudget, boolean dureeRequise) {
        this.demande = demande;
        this.demandeur = demandeur;
        this.dateReceptionFr = dateReceptionFr;
        this.controleBudget = controleBudget;
        this.dureeRequise = dureeRequise;
    }

    public DemandeBesoinModel getDemande() { return demande; }
    public String getDemandeur() { return demandeur; }
    public String getDateReceptionFr() { return dateReceptionFr; }
    public ControleBudget getControleBudget() { return controleBudget; }
    public boolean isDureeRequise() { return dureeRequise; }
}
