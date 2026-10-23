package com.gestionrh.sirh.model;

import jakarta.persistence.*;

@Entity
@Table(name = "poste")
public class PosteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_poste")
    private Integer id;

    @Column(name = "designation", nullable = false)
    private String designation;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

}
