package com.gestionrh.sirh.model;

import jakarta.persistence.*;

@Entity
@Table(name = "type_contrat")
public class TypeContratModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_contrat")
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public boolean isCdd() { return "CDD".equalsIgnoreCase(code); }

    public boolean isAvecDuree() {
        return "CDD".equalsIgnoreCase(code) || "CE".equalsIgnoreCase(code) || "STAGE".equalsIgnoreCase(code);
    }
}
