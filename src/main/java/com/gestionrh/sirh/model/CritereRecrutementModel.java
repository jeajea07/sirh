package com.gestionrh.sirh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "critere_recrutement")
public class CritereRecrutementModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_critere_recrutement")
    private Integer id;

    @Column(name = "niveau_etude")
    private String niveauEtude;

    @Column(name = "annee_experience")
    private Integer anneeExperience;

    @Column(name = "missions")
    private String missions;

    @Column(name = "competence")
    private String competence;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getMissions() {
        return missions;
    }

    public void setMissions(String missions) {
        this.missions = missions;
    }

    public String getCompetence() {
        return competence;
    }

    public void setCompetence(String competence) {
        this.competence = competence;
    }

    public String getNiveauEtudeLibelle() {
        if (niveauEtude == null) {
            return "-";
        }
        if (niveauEtude.equals("LICENCE")) {
            return "Licence";
        }
        if (niveauEtude.equals("MASTER")) {
            return "Master";
        }
        if (niveauEtude.equals("PHD")) {
            return "Doctorat (PhD)";
        }
        return niveauEtude;
    }
}
