package com.gestionrh.sirh.model;

import com.gestionrh.sirh.util.Formats;
import java.math.BigDecimal;
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

/** Mouvement de solde : le solde en est la somme (RG-SC03). */
@Entity
@Table(name = "mouvement_solde")
public class MouvementSoldeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mouvement_solde")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "type_solde", nullable = false)
    private String typeSolde;

    @Column(name = "type_mouvement", nullable = false)
    private String typeMouvement;

    @Column(name = "jours", nullable = false)
    private BigDecimal jours;

    @Column(name = "id_droit_conge")
    private Integer idDroitConge;

    @Column(name = "id_demande_conge")
    private Integer idDemandeConge;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_auteur")
    private EmployeModel auteur;

    @Column(name = "motif")
    private String motif;

    @Column(name = "date_mouvement", nullable = false)
    private LocalDateTime dateMouvement = java.time.LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public String getTypeSolde() { return typeSolde; }
    public void setTypeSolde(String typeSolde) { this.typeSolde = typeSolde; }

    public String getTypeMouvement() { return typeMouvement; }
    public void setTypeMouvement(String typeMouvement) { this.typeMouvement = typeMouvement; }

    public BigDecimal getJours() { return jours; }
    public void setJours(BigDecimal jours) { this.jours = jours; }

    public Integer getIdDroitConge() { return idDroitConge; }
    public void setIdDroitConge(Integer idDroitConge) { this.idDroitConge = idDroitConge; }

    public Integer getIdDemandeConge() { return idDemandeConge; }
    public void setIdDemandeConge(Integer idDemandeConge) { this.idDemandeConge = idDemandeConge; }

    public EmployeModel getAuteur() { return auteur; }
    public void setAuteur(EmployeModel auteur) { this.auteur = auteur; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public LocalDateTime getDateMouvement() { return dateMouvement; }
    public void setDateMouvement(LocalDateTime dateMouvement) { this.dateMouvement = dateMouvement; }

    public String getDateFr() { return Formats.dateHeure(dateMouvement); }
    public String getTypeLibelle() {
        return switch (typeMouvement) {
            case "CREDIT" -> "Acquisition"; case "DEBIT" -> "Congé pris";
            case "EXPIRATION" -> "Expiration"; default -> "Ajustement";
        };
    }
}
