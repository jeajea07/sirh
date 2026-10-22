package com.gestionrh.sirh.model.recrutement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Competence declaree dans le CV numerique. */
@Entity
@Table(name = "cv_competence")
public class CvCompetence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCvCompetence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @Column(nullable = false, length = 100)
    private String libelle;

    public Integer getIdCvCompetence() { return idCvCompetence; }
    public void setIdCvCompetence(Integer idCvCompetence) { this.idCvCompetence = idCvCompetence; }

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
}
