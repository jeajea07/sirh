package com.gestionrh.sirh.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Régime de travail (Bloc 6 §5.1). */
@Entity
@Table(name = "regime_travail")
public class RegimeTravailModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regime_travail")
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "duree_hebdo_heures", nullable = false)
    private BigDecimal dureeHebdoHeures;

    @Column(name = "jours_travailles", nullable = false)
    private String joursTravailles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_modele_horaire_defaut", nullable = false)
    private ModeleHoraireModel modeleDefaut;

    @Column(name = "pointage_obligatoire", nullable = false)
    private boolean pointageObligatoire;

    @Column(name = "heures_sup_autorisees", nullable = false)
    private boolean heuresSupAutorisees;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public BigDecimal getDureeHebdoHeures() { return dureeHebdoHeures; }
    public void setDureeHebdoHeures(BigDecimal dureeHebdoHeures) { this.dureeHebdoHeures = dureeHebdoHeures; }

    public String getJoursTravailles() { return joursTravailles; }
    public void setJoursTravailles(String joursTravailles) { this.joursTravailles = joursTravailles; }

    public ModeleHoraireModel getModeleDefaut() { return modeleDefaut; }
    public void setModeleDefaut(ModeleHoraireModel modeleDefaut) { this.modeleDefaut = modeleDefaut; }

    public boolean isPointageObligatoire() { return pointageObligatoire; }
    public void setPointageObligatoire(boolean pointageObligatoire) { this.pointageObligatoire = pointageObligatoire; }

    public boolean isHeuresSupAutorisees() { return heuresSupAutorisees; }
    public void setHeuresSupAutorisees(boolean heuresSupAutorisees) { this.heuresSupAutorisees = heuresSupAutorisees; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    /** 1 = lundi … 7 = dimanche. */
    public boolean travaille(int jourSemaine) {
        for (String j : joursTravailles.split(",")) {
            if (j.trim().equals(String.valueOf(jourSemaine))) return true;
        }
        return false;
    }
    public int getNombreJoursTravailles() { return joursTravailles.split(",").length; }
    /** Durée journalière de référence : durée hebdomadaire / jours travaillés. */
    public int getMinutesJournalieres() {
        return dureeHebdoHeures.multiply(java.math.BigDecimal.valueOf(60))
                .divide(java.math.BigDecimal.valueOf(getNombreJoursTravailles()), 0, java.math.RoundingMode.HALF_UP).intValue();
    }
}
