package com.gestionrh.sirh.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "validation_etape")
public class ValidationEtapeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validation_etape")
    private Integer id;

    @Column(name = "id_demande_besoin", nullable = false)
    private Integer idDemandeBesoin;

    @Column(name = "etape", nullable = false)
    private String etape;

    @Column(name = "id_validateur")
    private Integer idValidateur;

    @Column(name = "decision")
    private String decision;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "date_reception", nullable = false)
    private LocalDateTime dateReception;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdDemandeBesoin() { return idDemandeBesoin; }
    public void setIdDemandeBesoin(Integer idDemandeBesoin) { this.idDemandeBesoin = idDemandeBesoin; }

    public String getEtape() { return etape; }
    public void setEtape(String etape) { this.etape = etape; }

    public Integer getIdValidateur() { return idValidateur; }
    public void setIdValidateur(Integer idValidateur) { this.idValidateur = idValidateur; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateReception() { return dateReception; }
    public void setDateReception(LocalDateTime dateReception) { this.dateReception = dateReception; }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }

}
