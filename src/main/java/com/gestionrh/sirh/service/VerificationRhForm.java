package com.gestionrh.sirh.service;

import java.math.BigDecimal;

public class VerificationRhForm {

    private BigDecimal salairePropose;
    private Integer dureeMois;
    private String niveauEtude;
    private Integer anneeExperience;
    private String competence;
    private String missions;
    private String commentaire;

    public BigDecimal getSalairePropose() { return salairePropose; }
    public void setSalairePropose(BigDecimal salairePropose) { this.salairePropose = salairePropose; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public String getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(String niveauEtude) { this.niveauEtude = niveauEtude; }

    public Integer getAnneeExperience() { return anneeExperience; }
    public void setAnneeExperience(Integer anneeExperience) { this.anneeExperience = anneeExperience; }

    public String getCompetence() { return competence; }
    public void setCompetence(String competence) { this.competence = competence; }

    public String getMissions() { return missions; }
    public void setMissions(String missions) { this.missions = missions; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
