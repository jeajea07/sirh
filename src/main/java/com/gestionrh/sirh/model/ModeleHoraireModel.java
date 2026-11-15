package com.gestionrh.sirh.model;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Journée type (Bloc 6 §5.2). */
@Entity
@Table(name = "modele_horaire")
public class ModeleHoraireModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modele_horaire")
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "heure_debut", nullable = false)
    private LocalTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalTime heureFin;

    @Column(name = "duree_pause_minutes", nullable = false)
    private int dureePauseMinutes;

    @Column(name = "tolerance_retard_minutes", nullable = false)
    private int toleranceRetardMinutes;

    @Column(name = "tolerance_depart_minutes", nullable = false)
    private int toleranceDepartMinutes;

    @Column(name = "est_nuit", nullable = false)
    private boolean estNuit;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }

    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }

    public int getDureePauseMinutes() { return dureePauseMinutes; }
    public void setDureePauseMinutes(int dureePauseMinutes) { this.dureePauseMinutes = dureePauseMinutes; }

    public int getToleranceRetardMinutes() { return toleranceRetardMinutes; }
    public void setToleranceRetardMinutes(int toleranceRetardMinutes) { this.toleranceRetardMinutes = toleranceRetardMinutes; }

    public int getToleranceDepartMinutes() { return toleranceDepartMinutes; }
    public void setToleranceDepartMinutes(int toleranceDepartMinutes) { this.toleranceDepartMinutes = toleranceDepartMinutes; }

    public boolean isEstNuit() { return estNuit; }
    public void setEstNuit(boolean estNuit) { this.estNuit = estNuit; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    /** Durée théorique en minutes : fin − début − pause (RG-RT05 : un horaire de nuit chevauche minuit). */
    public int getDureeMinutes() {
        int debut = heureDebut.toSecondOfDay() / 60;
        int fin = heureFin.toSecondOfDay() / 60;
        int brut = fin > debut ? fin - debut : fin + 24 * 60 - debut;
        return Math.max(0, brut - dureePauseMinutes);
    }
    public String getDureeLibelle() { return getDureeMinutes() / 60 + " h" + (getDureeMinutes() % 60 == 0 ? "" : " " + getDureeMinutes() % 60); }
    public String getPlageLibelle() { return heureDebut + " – " + heureFin; }
}
