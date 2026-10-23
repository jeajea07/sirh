package com.gestionrh.sirh.service.dto;

import java.time.LocalDate;

public class DeclarationForm {

    private Integer id;
    private Integer idEmploye;
    private Integer idOrganisme;
    private String typeDeclaration;
    private String referenceExterne;
    private String commentaire;
    private LocalDate datePreparation;
    private LocalDate dateEnvoi;
    private Integer idDocumentJustificatif;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }
    public Integer getIdOrganisme() { return idOrganisme; }
    public void setIdOrganisme(Integer idOrganisme) { this.idOrganisme = idOrganisme; }
    public String getTypeDeclaration() { return typeDeclaration; }
    public void setTypeDeclaration(String typeDeclaration) { this.typeDeclaration = typeDeclaration; }
    public String getReferenceExterne() { return referenceExterne; }
    public void setReferenceExterne(String referenceExterne) { this.referenceExterne = referenceExterne; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDate getDatePreparation() { return datePreparation; }
    public void setDatePreparation(LocalDate datePreparation) { this.datePreparation = datePreparation; }
    public LocalDate getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDate dateEnvoi) { this.dateEnvoi = dateEnvoi; }
    public Integer getIdDocumentJustificatif() { return idDocumentJustificatif; }
    public void setIdDocumentJustificatif(Integer idDocumentJustificatif) { this.idDocumentJustificatif = idDocumentJustificatif; }
}
