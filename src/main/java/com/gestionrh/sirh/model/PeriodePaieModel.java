package com.gestionrh.sirh.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Mois de paie, clôturé une seule fois (RG-VP02). */
@Entity
@Table(name = "periode_paie")
public class PeriodePaieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_periode_paie")
    private Integer id;

    @Column(name = "annee", nullable = false)
    private int annee;

    @Column(name = "mois", nullable = false)
    private int mois;

    @Column(name = "statut", nullable = false)
    private String statut = "OUVERTE";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_cloture_par")
    private EmployeModel cloturePar;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }

    public int getMois() { return mois; }
    public void setMois(int mois) { this.mois = mois; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public EmployeModel getCloturePar() { return cloturePar; }
    public void setCloturePar(EmployeModel cloturePar) { this.cloturePar = cloturePar; }

    public LocalDateTime getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }

    public boolean isCloturee() { return "CLOTUREE".equals(statut); }
    public java.time.LocalDate getPremierJour() { return java.time.LocalDate.of(annee, mois, 1); }
    public java.time.LocalDate getDernierJour() { return getPremierJour().plusMonths(1).minusDays(1); }
    public String getLibelle() {
        return getPremierJour().getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH) + " " + annee;
    }
}
