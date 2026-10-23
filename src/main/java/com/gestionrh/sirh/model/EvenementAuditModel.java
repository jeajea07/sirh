package com.gestionrh.sirh.model;

import java.time.LocalDateTime;

import com.gestionrh.sirh.util.Formats;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "evenement_audit")
public class EvenementAuditModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evenement_audit")
    private Integer id;

    @Column(name = "type_objet", nullable = false)
    private String typeObjet;

    @Column(name = "id_objet", nullable = false)
    private Integer idObjet;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "ancienne_valeur")
    private String ancienneValeur;

    @Column(name = "nouvelle_valeur")
    private String nouvelleValeur;

    @Column(name = "commentaire")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_auteur", nullable = false)
    private EmployeModel auteur;

    @Column(name = "date_evenement", nullable = false)
    private LocalDateTime dateEvenement;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTypeObjet() { return typeObjet; }
    public void setTypeObjet(String typeObjet) { this.typeObjet = typeObjet; }

    public Integer getIdObjet() { return idObjet; }
    public void setIdObjet(Integer idObjet) { this.idObjet = idObjet; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }

    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public EmployeModel getAuteur() { return auteur; }
    public void setAuteur(EmployeModel auteur) { this.auteur = auteur; }

    public LocalDateTime getDateEvenement() { return dateEvenement; }
    public void setDateEvenement(LocalDateTime dateEvenement) { this.dateEvenement = dateEvenement; }

    public String getDateEvenementFr() { return Formats.dateHeure(dateEvenement); }
}
