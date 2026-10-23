package com.gestionrh.sirh.dto;

/** Une décision prise par le RH sur une demande (ligne de la page « Historique »). */
public class HistoriqueRhLigne {

    private final Integer idDemande;
    private final String numero;
    private final String poste;
    private final String departement;
    private final String demandeur;
    private final String contrat;
    private final String salaireFr;
    private final String decision;
    private final String commentaire;
    private final String dateDecisionFr;
    private final String statutActuel;
    private final String statutBadge;

    public HistoriqueRhLigne(Integer idDemande, String numero, String poste, String departement, String demandeur,
                             String contrat, String salaireFr, String decision, String commentaire,
                             String dateDecisionFr, String statutActuel, String statutBadge) {
        this.idDemande = idDemande;
        this.numero = numero;
        this.poste = poste;
        this.departement = departement;
        this.demandeur = demandeur;
        this.contrat = contrat;
        this.salaireFr = salaireFr;
        this.decision = decision;
        this.commentaire = commentaire;
        this.dateDecisionFr = dateDecisionFr;
        this.statutActuel = statutActuel;
        this.statutBadge = statutBadge;
    }

    public Integer getIdDemande() { return idDemande; }
    public String getNumero() { return numero; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getDemandeur() { return demandeur; }
    public String getContrat() { return contrat; }
    public String getSalaireFr() { return salaireFr; }
    public String getDecision() { return decision; }
    public String getCommentaire() { return commentaire; }
    public String getDateDecisionFr() { return dateDecisionFr; }
    public String getStatutActuel() { return statutActuel; }
    public String getStatutBadge() { return statutBadge; }

    public String getDecisionLibelle() {
        if ("VALIDEE".equals(decision)) { return "Validée"; }
        if ("RENVOYEE".equals(decision)) { return "Renvoyée"; }
        if ("REJETEE".equals(decision)) { return "Rejetée"; }
        return decision;
    }

    public String getDecisionBadge() {
        if ("VALIDEE".equals(decision)) { return "badge-ok"; }
        if ("RENVOYEE".equals(decision)) { return "badge-warn"; }
        return "badge-bad";
    }
}
