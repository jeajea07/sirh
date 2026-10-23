package com.gestionrh.sirh.service;

import java.util.List;

import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.service.dto.ControleSalaire;

public class VerificationRh {

    private final DemandeBesoinModel demande;
    private final String demandeur;
    private final String dateReceptionFr;
    private final ControleSalaire controleSalaire;
    private final List<String> alertes;
    private final boolean dureeRequise;

    public VerificationRh(DemandeBesoinModel demande, String demandeur, String dateReceptionFr,
                          ControleSalaire controleSalaire, List<String> alertes, boolean dureeRequise) {
        this.demande = demande;
        this.demandeur = demandeur;
        this.dateReceptionFr = dateReceptionFr;
        this.controleSalaire = controleSalaire;
        this.alertes = alertes;
        this.dureeRequise = dureeRequise;
    }

    public DemandeBesoinModel getDemande() { return demande; }
    public String getDemandeur() { return demandeur; }
    public String getDateReceptionFr() { return dateReceptionFr; }
    public ControleSalaire getControleSalaire() { return controleSalaire; }
    public List<String> getAlertes() { return alertes; }
    public boolean isDureeRequise() { return dureeRequise; }
}
