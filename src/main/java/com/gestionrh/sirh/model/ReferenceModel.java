// Bloc 3
package com.gestionrh.sirh.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ReferenceModel {
    @Id
    private Integer id;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public String getLibelle() { return libelle; }
}
