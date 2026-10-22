package com.gestionrh.sirh.model.recrutement;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** Historique des decisions. idAuteur == null : action automatique du systeme. */
@Entity
@Table(name = "historique_candidature")
public class HistoriqueCandidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idHistoriqueCandidature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidature", nullable = false)
    private Candidature candidature;

    @Column(nullable = false, length = 40)
    private String action;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CandidatureStatut ancienStatut;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CandidatureStatut nouveauStatut;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Classement ancienClassement;

    @Enumerated(EnumType.STRING)
    @Column(length = 15)
    private Classement nouveauClassement;

    private Integer idAuteur;

    @Column(columnDefinition = "text")
    private String commentaire;

    @Column(nullable = false)
    private LocalDateTime dateAction = LocalDateTime.now();

    public Integer getIdHistoriqueCandidature() { return idHistoriqueCandidature; }
    public void setIdHistoriqueCandidature(Integer idHistoriqueCandidature) { this.idHistoriqueCandidature = idHistoriqueCandidature; }

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public CandidatureStatut getAncienStatut() { return ancienStatut; }
    public void setAncienStatut(CandidatureStatut ancienStatut) { this.ancienStatut = ancienStatut; }

    public CandidatureStatut getNouveauStatut() { return nouveauStatut; }
    public void setNouveauStatut(CandidatureStatut nouveauStatut) { this.nouveauStatut = nouveauStatut; }

    public Classement getAncienClassement() { return ancienClassement; }
    public void setAncienClassement(Classement ancienClassement) { this.ancienClassement = ancienClassement; }

    public Classement getNouveauClassement() { return nouveauClassement; }
    public void setNouveauClassement(Classement nouveauClassement) { this.nouveauClassement = nouveauClassement; }

    public Integer getIdAuteur() { return idAuteur; }
    public void setIdAuteur(Integer idAuteur) { this.idAuteur = idAuteur; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }
}
