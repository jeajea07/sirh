package com.gestionrh.sirh.service.dto;

public class DocumentForm {

    private String typeDocument;
    private String titre;
    private String niveauConfidentialite = "INTERNE";
    private String commentaire;
    private Integer idEmploye;
    private Integer idContrat;
    private Integer idOnboardingTache;

    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public String getNiveauConfidentialite() { return niveauConfidentialite; }
    public void setNiveauConfidentialite(String niveauConfidentialite) { this.niveauConfidentialite = niveauConfidentialite; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }
    public Integer getIdContrat() { return idContrat; }
    public void setIdContrat(Integer idContrat) { this.idContrat = idContrat; }
    public Integer getIdOnboardingTache() { return idOnboardingTache; }
    public void setIdOnboardingTache(Integer idOnboardingTache) { this.idOnboardingTache = idOnboardingTache; }
}
