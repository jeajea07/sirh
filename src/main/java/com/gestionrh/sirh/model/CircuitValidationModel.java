package com.gestionrh.sirh.model;

import jakarta.persistence.*;

@Entity
@Table(name = "circuit_validation")
public class CircuitValidationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_circuit_validation")
    private Integer id;

    @Column(name = "etape", nullable = false)
    private String etape;

    @Column(name = "role", nullable = false)
    private String role;

    @Column(name = "obligatoire", nullable = false)
    private boolean obligatoire = true;

    @Column(name = "delai_jours", nullable = false)
    private Integer delaiJours = 5;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getEtape() { return etape; }
    public void setEtape(String etape) { this.etape = etape; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }

    public Integer getDelaiJours() { return delaiJours; }
    public void setDelaiJours(Integer delaiJours) { this.delaiJours = delaiJours; }

}
