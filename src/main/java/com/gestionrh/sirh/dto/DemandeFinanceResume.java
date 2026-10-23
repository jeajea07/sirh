package com.gestionrh.sirh.dto;

public class DemandeFinanceResume {

    private final Integer id;
    private final String numero;
    private final String poste;
    private final String departement;
    private final String demandeur;
    private final String contrat;
    private final String motif;
    private final String dateReceptionFr;
    private final ControleBudget controleBudget;

    public DemandeFinanceResume(Integer id, String numero, String poste, String departement, String demandeur,
                                String contrat, String motif, String dateReceptionFr, ControleBudget controleBudget) {
        this.id = id;
        this.numero = numero;
        this.poste = poste;
        this.departement = departement;
        this.demandeur = demandeur;
        this.contrat = contrat;
        this.motif = motif;
        this.dateReceptionFr = dateReceptionFr;
        this.controleBudget = controleBudget;
    }

    public Integer getId() { return id; }
    public String getNumero() { return numero; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getDemandeur() { return demandeur; }
    public String getContrat() { return contrat; }
    public String getMotif() { return motif; }
    public String getDateReceptionFr() { return dateReceptionFr; }
    public ControleBudget getControleBudget() { return controleBudget; }
}
