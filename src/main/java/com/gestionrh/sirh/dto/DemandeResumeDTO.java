package com.gestionrh.sirh.dto;

public class DemandeResumeDTO {

    private final Integer id;
    private final String numero;
    private final String posteDesignation;
    private final String motifLibelle;
    private final String dateCreationFr;
    private final String dateSouhaiteeFr;
    private final String statutLibelle;
    private final String statutBadge;

    public DemandeResumeDTO(Integer id, String numero, String posteDesignation, String motifLibelle,
                            String dateCreationFr, String dateSouhaiteeFr, String statutLibelle, String statutBadge) {
        this.id = id;
        this.numero = numero;
        this.posteDesignation = posteDesignation;
        this.motifLibelle = motifLibelle;
        this.dateCreationFr = dateCreationFr;
        this.dateSouhaiteeFr = dateSouhaiteeFr;
        this.statutLibelle = statutLibelle;
        this.statutBadge = statutBadge;
    }

    public Integer getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public String getPosteDesignation() {
        return posteDesignation;
    }

    public String getMotifLibelle() {
        return motifLibelle;
    }

    public String getDateCreationFr() {
        return dateCreationFr;
    }

    public String getDateSouhaiteeFr() {
        return dateSouhaiteeFr;
    }

    public String getStatutLibelle() {
        return statutLibelle;
    }

    public String getStatutBadge() {
        return statutBadge;
    }
}
