package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

public class DemandeForm {

    private Integer id;
    private Integer departementId;

    private Integer idPoste;
    private Integer nombrePostes = 1;
    private String motif;
    private String niveauUrgence = "URGENT";
    private Integer idTypeContrat;
    private Integer dureeMois;
    private BigDecimal salairePropose;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateSouhaitee;

    private String justification;

    private String niveauEtude;
    private Integer anneeExperience;
    private String competence;
    private String missions;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDepartementId() {
        return departementId;
    }

    public void setDepartementId(Integer departementId) {
        this.departementId = departementId;
    }

    public Integer getIdPoste() {
        return idPoste;
    }

    public void setIdPoste(Integer idPoste) {
        this.idPoste = idPoste;
    }

    public Integer getNombrePostes() {
        return nombrePostes;
    }

    public void setNombrePostes(Integer nombrePostes) {
        this.nombrePostes = nombrePostes;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getNiveauUrgence() {
        return niveauUrgence;
    }

    public void setNiveauUrgence(String niveauUrgence) {
        this.niveauUrgence = niveauUrgence;
    }

    public Integer getIdTypeContrat() {
        return idTypeContrat;
    }

    public void setIdTypeContrat(Integer idTypeContrat) {
        this.idTypeContrat = idTypeContrat;
    }

    public Integer getDureeMois() {
        return dureeMois;
    }

    public void setDureeMois(Integer dureeMois) {
        this.dureeMois = dureeMois;
    }

    public BigDecimal getSalairePropose() {
        return salairePropose;
    }

    public void setSalairePropose(BigDecimal salairePropose) {
        this.salairePropose = salairePropose;
    }

    public LocalDate getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDate dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getNiveauEtude() {
        return niveauEtude;
    }

    public void setNiveauEtude(String niveauEtude) {
        this.niveauEtude = niveauEtude;
    }

    public Integer getAnneeExperience() {
        return anneeExperience;
    }

    public void setAnneeExperience(Integer anneeExperience) {
        this.anneeExperience = anneeExperience;
    }

    public String getCompetence() {
        return competence;
    }

    public void setCompetence(String competence) {
        this.competence = competence;
    }

    public String getMissions() {
        return missions;
    }

    public void setMissions(String missions) {
        this.missions = missions;
    }
}
