package com.gestionrh.sirh.service.dto;

public class SignatureVue {

    private final Integer id;
    private final String partieLibelle;
    private final String nomSignataire;
    private final String dateSignatureFr;
    private final String statutLibelle;
    private final String statutBadge;
    private final String commentaire;

    public SignatureVue(Integer id, String partieLibelle, String nomSignataire,
                        String dateSignatureFr, String statutLibelle, String statutBadge,
                        String commentaire) {
        this.id = id;
        this.partieLibelle = partieLibelle;
        this.nomSignataire = nomSignataire;
        this.dateSignatureFr = dateSignatureFr;
        this.statutLibelle = statutLibelle;
        this.statutBadge = statutBadge;
        this.commentaire = commentaire;
    }

    public Integer getId() { return id; }
    public String getPartieLibelle() { return partieLibelle; }
    public String getNomSignataire() { return nomSignataire; }
    public String getDateSignatureFr() { return dateSignatureFr; }
    public String getStatutLibelle() { return statutLibelle; }
    public String getStatutBadge() { return statutBadge; }
    public String getCommentaire() { return commentaire; }
}
