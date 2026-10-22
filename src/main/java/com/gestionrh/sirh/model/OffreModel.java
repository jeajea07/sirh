package com.gestionrh.sirh.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "offre")
public class OffreModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_offre")
    private Long idOffre;

    @Column(name = "numero_offre", nullable = false, unique = true)
    private String numeroOffre;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "nombre_postes")
    private Integer nombrePostes = 1;

    @Column(name = "date_publication")
    private LocalDate datePublication;

    @Column(name = "date_limite")
    private LocalDate dateLimite;

    @Column(name = "statut")
    private String statut = "OUVERTE";

    public Long getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(Long idOffre) {
        this.idOffre = idOffre;
    }

    public String getNumeroOffre() {
        return numeroOffre;
    }

    public void setNumeroOffre(String numeroOffre) {
        this.numeroOffre = numeroOffre;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public Integer getNombrePostes() {
        return nombrePostes;
    }

    public void setNombrePostes(Integer nombrePostes) {
        this.nombrePostes = nombrePostes;
    }

    public LocalDate getDatePublication() {
        return datePublication;
    }

    public void setDatePublication(LocalDate datePublication) {
        this.datePublication = datePublication;
    }

    public LocalDate getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDate dateLimite) {
        this.dateLimite = dateLimite;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}
