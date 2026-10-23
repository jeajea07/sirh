package com.gestionrh.sirh.service.dto;

public class ValidationVue {

    private final Integer id;
    private final String etapeLibelle;
    private final String validateurNom;
    private final String decisionLibelle;
    private final String decisionBadge;
    private final String commentaire;
    private final String dateReceptionFr;
    private final String dateDecisionFr;

    public ValidationVue(Integer id, String etapeLibelle, String validateurNom,
                         String decisionLibelle, String decisionBadge, String commentaire,
                         String dateReceptionFr, String dateDecisionFr) {
        this.id = id;
        this.etapeLibelle = etapeLibelle;
        this.validateurNom = validateurNom;
        this.decisionLibelle = decisionLibelle;
        this.decisionBadge = decisionBadge;
        this.commentaire = commentaire;
        this.dateReceptionFr = dateReceptionFr;
        this.dateDecisionFr = dateDecisionFr;
    }

    public Integer getId() { return id; }
    public String getEtapeLibelle() { return etapeLibelle; }
    public String getValidateurNom() { return validateurNom; }
    public String getDecisionLibelle() { return decisionLibelle; }
    public String getDecisionBadge() { return decisionBadge; }
    public String getCommentaire() { return commentaire; }
    public String getDateReceptionFr() { return dateReceptionFr; }
    public String getDateDecisionFr() { return dateDecisionFr; }
}
