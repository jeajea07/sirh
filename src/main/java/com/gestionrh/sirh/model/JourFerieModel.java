package com.gestionrh.sirh.model;

import com.gestionrh.sirh.util.Formats;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Jour férié paramétré par le RH. */
@Entity
@Table(name = "jour_ferie")
public class JourFerieModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_jour_ferie")
    private Integer id;

    @Column(name = "jour", nullable = false)
    private LocalDate jour;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public LocalDate getJour() { return jour; }
    public void setJour(LocalDate jour) { this.jour = jour; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getJourFr() { return Formats.date(jour); }
}
