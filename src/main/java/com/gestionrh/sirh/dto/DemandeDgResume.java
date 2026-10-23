package com.gestionrh.sirh.dto;

import com.gestionrh.sirh.model.DemandeBesoinModel;

public class DemandeDgResume {
    private final DemandeBesoinModel demande;
    private final String demandeur;

    public DemandeDgResume(DemandeBesoinModel demande, String demandeur) {
        this.demande = demande;
        this.demandeur = demandeur;
    }

    public DemandeBesoinModel getDemande() { return demande; }
    public String getDemandeur() { return demandeur; }
}
