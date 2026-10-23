package com.gestionrh.sirh.service.dto;

public class EvenementAuditVue {

    private final Integer id;
    private final String action;
    private final String ancienneValeur;
    private final String nouvelleValeur;
    private final String commentaire;
    private final String auteurNom;
    private final String dateEvenementFr;

    public EvenementAuditVue(Integer id, String action, String ancienneValeur,
                             String nouvelleValeur, String commentaire, String auteurNom,
                             String dateEvenementFr) {
        this.id = id;
        this.action = action;
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.commentaire = commentaire;
        this.auteurNom = auteurNom;
        this.dateEvenementFr = dateEvenementFr;
    }

    public Integer getId() { return id; }
    public String getAction() { return action; }
    public String getAncienneValeur() { return ancienneValeur; }
    public String getNouvelleValeur() { return nouvelleValeur; }
    public String getCommentaire() { return commentaire; }
    public String getAuteurNom() { return auteurNom; }
    public String getDateEvenementFr() { return dateEvenementFr; }
}
