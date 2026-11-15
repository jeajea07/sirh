package com.gestionrh.sirh.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Régime d'un salarié sur une période (RG-RT01, RG-RT04). */
@Entity
@Table(name = "regime_salarie")
public class RegimeSalarieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_regime_salarie")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_regime_travail", nullable = false)
    private RegimeTravailModel regime;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public RegimeTravailModel getRegime() { return regime; }
    public void setRegime(RegimeTravailModel regime) { this.regime = regime; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
}
