package com.gestionrh.sirh.service.dto;

public class DeclarationResume {

    private final Integer id;
    private final Integer idEmploye;
    private final String employeNom;
    private final String organisme;
    private final String typeDeclaration;
    private final String datePreparationFr;
    private final String dateEnvoiFr;
    private final String referenceExterne;
    private final String statut;
    private final String statutLibelle;
    private final String statutBadge;
    private final String commentaire;
    private final Integer idDocumentJustificatif;

    public DeclarationResume(Integer id, Integer idEmploye, String employeNom, String organisme,
                             String typeDeclaration, String datePreparationFr, String dateEnvoiFr,
                             String referenceExterne, String statut, String statutLibelle,
                             String statutBadge, String commentaire, Integer idDocumentJustificatif) {
        this.id = id;
        this.idEmploye = idEmploye;
        this.employeNom = employeNom;
        this.organisme = organisme;
        this.typeDeclaration = typeDeclaration;
        this.datePreparationFr = datePreparationFr;
        this.dateEnvoiFr = dateEnvoiFr;
        this.referenceExterne = referenceExterne;
        this.statut = statut;
        this.statutLibelle = statutLibelle;
        this.statutBadge = statutBadge;
        this.commentaire = commentaire;
        this.idDocumentJustificatif = idDocumentJustificatif;
    }

    public Integer getId() { return id; }
    public Integer getIdEmploye() { return idEmploye; }
    public String getEmployeNom() { return employeNom; }
    public String getOrganisme() { return organisme; }
    public String getTypeDeclaration() { return typeDeclaration; }
    public String getDatePreparationFr() { return datePreparationFr; }
    public String getDateEnvoiFr() { return dateEnvoiFr; }
    public String getReferenceExterne() { return referenceExterne; }
    public String getStatut() { return statut; }
    public String getStatutLibelle() { return statutLibelle; }
    public String getStatutBadge() { return statutBadge; }
    public String getCommentaire() { return commentaire; }
    public Integer getIdDocumentJustificatif() { return idDocumentJustificatif; }
}
