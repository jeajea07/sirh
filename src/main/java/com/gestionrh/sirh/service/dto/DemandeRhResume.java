package com.gestionrh.sirh.service.dto;

public class DemandeRhResume {

    private final Integer id;
    private final String numero;
    private final String poste;
    private final String departement;
    private final String demandeur;
    private final String contrat;
    private final String urgence;
    private final String dateReceptionFr;
    private final ControleSalaire controleSalaire;

    public DemandeRhResume(Integer id, String numero, String poste, String departement, String demandeur,
                           String contrat, String urgence, String dateReceptionFr, ControleSalaire controleSalaire) {
        this.id = id;
        this.numero = numero;
        this.poste = poste;
        this.departement = departement;
        this.demandeur = demandeur;
        this.contrat = contrat;
        this.urgence = urgence;
        this.dateReceptionFr = dateReceptionFr;
        this.controleSalaire = controleSalaire;
    }

    public Integer getId() { return id; }
    public String getNumero() { return numero; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getDemandeur() { return demandeur; }
    public String getContrat() { return contrat; }
    public String getUrgence() { return urgence; }
    public String getDateReceptionFr() { return dateReceptionFr; }
    public ControleSalaire getControleSalaire() { return controleSalaire; }
}
