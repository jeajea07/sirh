package com.gestionrh.sirh.service.dto;

public class ContratVue {

    private Integer id;
    private String reference;
    private Integer idEmploye;
    private String employeNom;
    private String employeMatricule;
    private String employeEmail;
    private String typeContratLibelle;
    private String typeOperationLibelle;
    private String dateSignatureFr;
    private String dateDebutFr;
    private String dateFinFr;
    private String fonction;
    private String categorieProfessionnelle;
    private String salaireFr;
    private String statutWorkflow;
    private String statutLibelle;
    private String statutBadge;
    private String statutContractuelLibelle;
    private String motifRecours;
    private String commentaire;
    private Integer version;
    private Long joursRestants;
    private Integer dureeMois;
    private String contratPrecedentReference;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }
    public String getEmployeNom() { return employeNom; }
    public void setEmployeNom(String employeNom) { this.employeNom = employeNom; }
    public String getEmployeMatricule() { return employeMatricule; }
    public void setEmployeMatricule(String employeMatricule) { this.employeMatricule = employeMatricule; }
    public String getEmployeEmail() { return employeEmail; }
    public void setEmployeEmail(String employeEmail) { this.employeEmail = employeEmail; }
    public String getTypeContratLibelle() { return typeContratLibelle; }
    public void setTypeContratLibelle(String typeContratLibelle) { this.typeContratLibelle = typeContratLibelle; }
    public String getTypeOperationLibelle() { return typeOperationLibelle; }
    public void setTypeOperationLibelle(String typeOperationLibelle) { this.typeOperationLibelle = typeOperationLibelle; }
    public String getDateSignatureFr() { return dateSignatureFr; }
    public void setDateSignatureFr(String dateSignatureFr) { this.dateSignatureFr = dateSignatureFr; }
    public String getDateDebutFr() { return dateDebutFr; }
    public void setDateDebutFr(String dateDebutFr) { this.dateDebutFr = dateDebutFr; }
    public String getDateFinFr() { return dateFinFr; }
    public void setDateFinFr(String dateFinFr) { this.dateFinFr = dateFinFr; }
    public String getFonction() { return fonction; }
    public void setFonction(String fonction) { this.fonction = fonction; }
    public String getCategorieProfessionnelle() { return categorieProfessionnelle; }
    public void setCategorieProfessionnelle(String categorieProfessionnelle) { this.categorieProfessionnelle = categorieProfessionnelle; }
    public String getSalaireFr() { return salaireFr; }
    public void setSalaireFr(String salaireFr) { this.salaireFr = salaireFr; }
    public String getStatutWorkflow() { return statutWorkflow; }
    public void setStatutWorkflow(String statutWorkflow) { this.statutWorkflow = statutWorkflow; }
    public String getStatutLibelle() { return statutLibelle; }
    public void setStatutLibelle(String statutLibelle) { this.statutLibelle = statutLibelle; }
    public String getStatutBadge() { return statutBadge; }
    public void setStatutBadge(String statutBadge) { this.statutBadge = statutBadge; }
    public String getStatutContractuelLibelle() { return statutContractuelLibelle; }
    public void setStatutContractuelLibelle(String statutContractuelLibelle) { this.statutContractuelLibelle = statutContractuelLibelle; }
    public String getMotifRecours() { return motifRecours; }
    public void setMotifRecours(String motifRecours) { this.motifRecours = motifRecours; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public Long getJoursRestants() { return joursRestants; }
    public void setJoursRestants(Long joursRestants) { this.joursRestants = joursRestants; }
    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }
    public String getContratPrecedentReference() { return contratPrecedentReference; }
    public void setContratPrecedentReference(String contratPrecedentReference) { this.contratPrecedentReference = contratPrecedentReference; }
}
