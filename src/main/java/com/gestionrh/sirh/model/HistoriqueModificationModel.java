package com.gestionrh.sirh.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "historique_modification")
public class HistoriqueModificationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique_modification")
    private Integer id;

    @Column(name = "id_demande_besoin", nullable = false)
    private Integer idDemandeBesoin;

    @Column(name = "id_auteur", nullable = false)
    private Integer idAuteur;

    @Column(name = "remarque")
    private String remarque;

    @Column(name = "date_modification", nullable = false)
    private LocalDateTime dateModification;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdDemandeBesoin() { return idDemandeBesoin; }
    public void setIdDemandeBesoin(Integer idDemandeBesoin) { this.idDemandeBesoin = idDemandeBesoin; }

    public Integer getIdAuteur() { return idAuteur; }
    public void setIdAuteur(Integer idAuteur) { this.idAuteur = idAuteur; }

    public String getRemarque() { return remarque; }
    public void setRemarque(String remarque) { this.remarque = remarque; }

    public LocalDateTime getDateModification() { return dateModification; }
    public void setDateModification(LocalDateTime dateModification) { this.dateModification = dateModification; }

}
