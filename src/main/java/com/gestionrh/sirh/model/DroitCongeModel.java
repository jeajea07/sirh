package com.gestionrh.sirh.model;

import java.math.BigDecimal;
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

/** Ligne d'acquisition de droits (Bloc 7 §6.1). */
@Entity
@Table(name = "droit_conge")
public class DroitCongeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_droit_conge")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "type_solde", nullable = false)
    private String typeSolde;

    @Column(name = "periode_reference", nullable = false)
    private LocalDate periodeReference;

    @Column(name = "jours", nullable = false)
    private BigDecimal jours;

    @Column(name = "origine", nullable = false)
    private String origine;

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = java.time.LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public String getTypeSolde() { return typeSolde; }
    public void setTypeSolde(String typeSolde) { this.typeSolde = typeSolde; }

    public LocalDate getPeriodeReference() { return periodeReference; }
    public void setPeriodeReference(LocalDate periodeReference) { this.periodeReference = periodeReference; }

    public BigDecimal getJours() { return jours; }
    public void setJours(BigDecimal jours) { this.jours = jours; }

    public String getOrigine() { return origine; }
    public void setOrigine(String origine) { this.origine = origine; }

    public LocalDate getDateExpiration() { return dateExpiration; }
    public void setDateExpiration(LocalDate dateExpiration) { this.dateExpiration = dateExpiration; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
