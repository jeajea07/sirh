package com.gestionrh.sirh.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
@Table(name = "budget_departement")
public class BudgetDepartementModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_budget_departement")
    private Integer id;

    @Column(name = "id_departement", nullable = false)
    private Integer idDepartement;

    /** Année du budget, par exemple « 2026 ». */
    @Column(name = "periode", nullable = false)
    private String periode;

    @Column(name = "montant_alloue", nullable = false)
    private BigDecimal montantAlloue;

    @Column(name = "montant_max", nullable = false)
    private BigDecimal montantMax;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdDepartement() { return idDepartement; }
    public void setIdDepartement(Integer idDepartement) { this.idDepartement = idDepartement; }

    public String getPeriode() { return periode; }
    public void setPeriode(String periode) { this.periode = periode; }

    public BigDecimal getMontantAlloue() { return montantAlloue; }
    public void setMontantAlloue(BigDecimal montantAlloue) { this.montantAlloue = montantAlloue; }

    public BigDecimal getMontantMax() { return montantMax; }
    public void setMontantMax(BigDecimal montantMax) { this.montantMax = montantMax; }
}
