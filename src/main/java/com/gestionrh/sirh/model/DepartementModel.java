package com.gestionrh.sirh.model;

import jakarta.persistence.*;

@Entity
@Table(name = "departement")
public class DepartementModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_departement")
    private Integer id;

    @Column(name = "designation", nullable = false)
    private String designation;

    @Column(name = "effectif")
    private Integer effectif;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public Integer getEffectif() { return effectif; }
    public void setEffectif(Integer effectif) { this.effectif = effectif; }

}
