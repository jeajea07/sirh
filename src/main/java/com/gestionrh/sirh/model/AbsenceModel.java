package com.gestionrh.sirh.model;

import com.gestionrh.sirh.util.Formats;
import java.time.LocalDate;
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

/** Absence déclarée, saisie ou constatée (Bloc 7 §9). */
@Entity
@Table(name = "absence")
public class AbsenceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_absence")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_conge", nullable = false)
    private TypeCongeModel type;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "origine", nullable = false)
    private String origine;

    @Column(name = "statut", nullable = false)
    private String statut = "DECLAREE";

    @Column(name = "id_justificatif")
    private Integer idJustificatif;

    @Column(name = "date_limite_justificatif")
    private LocalDate dateLimiteJustificatif;

    @Column(name = "id_demande_conge")
    private Integer idDemandeConge;

    @Column(name = "id_anomalie_pointage")
    private Integer idAnomaliePointage;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "decision_commentaire")
    private String decisionCommentaire;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_saisi_par", nullable = false)
    private EmployeModel saisiPar;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_decideur")
    private EmployeModel decideur;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = java.time.LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public TypeCongeModel getType() { return type; }
    public void setType(TypeCongeModel type) { this.type = type; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getOrigine() { return origine; }
    public void setOrigine(String origine) { this.origine = origine; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getIdJustificatif() { return idJustificatif; }
    public void setIdJustificatif(Integer idJustificatif) { this.idJustificatif = idJustificatif; }

    public LocalDate getDateLimiteJustificatif() { return dateLimiteJustificatif; }
    public void setDateLimiteJustificatif(LocalDate dateLimiteJustificatif) { this.dateLimiteJustificatif = dateLimiteJustificatif; }

    public Integer getIdDemandeConge() { return idDemandeConge; }
    public void setIdDemandeConge(Integer idDemandeConge) { this.idDemandeConge = idDemandeConge; }

    public Integer getIdAnomaliePointage() { return idAnomaliePointage; }
    public void setIdAnomaliePointage(Integer idAnomaliePointage) { this.idAnomaliePointage = idAnomaliePointage; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getDecisionCommentaire() { return decisionCommentaire; }
    public void setDecisionCommentaire(String decisionCommentaire) { this.decisionCommentaire = decisionCommentaire; }

    public EmployeModel getSaisiPar() { return saisiPar; }
    public void setSaisiPar(EmployeModel saisiPar) { this.saisiPar = saisiPar; }

    public EmployeModel getDecideur() { return decideur; }
    public void setDecideur(EmployeModel decideur) { this.decideur = decideur; }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public static final java.util.List<String> ACTIVES =
            java.util.List.of("DECLAREE", "EN_ATTENTE_JUSTIFICATIF", "JUSTIFIEE", "INJUSTIFIEE");

    public String getPeriodeLibelle() {
        return dateDebut.equals(dateFin) ? Formats.date(dateDebut) : Formats.date(dateDebut) + " → " + Formats.date(dateFin);
    }
    public long getNbJours() { return java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1; }
    public String getDateLimiteFr() { return Formats.date(dateLimiteJustificatif); }
    public boolean isEnAttente() { return "DECLAREE".equals(statut) || "EN_ATTENTE_JUSTIFICATIF".equals(statut); }
    public String getOrigineLibelle() {
        return switch (origine) {
            case "DECLAREE_SALARIE" -> "Déclarée par le salarié";
            case "SAISIE_RH" -> "Saisie par le RH";
            default -> "Constatée au pointage";
        };
    }
    public String getStatutLibelle() {
        return switch (statut) {
            case "DECLAREE" -> "Déclarée";
            case "EN_ATTENTE_JUSTIFICATIF" -> "Justificatif attendu";
            case "JUSTIFIEE" -> "Justifiée";
            case "INJUSTIFIEE" -> "Injustifiée";
            case "CONVERTIE" -> "Convertie en congé";
            case "ANNULEE" -> "Annulée";
            default -> statut;
        };
    }
    public String getStatutBadge() {
        return switch (statut) {
            case "JUSTIFIEE" -> "badge-ok";
            case "DECLAREE", "EN_ATTENTE_JUSTIFICATIF" -> "badge-warn";
            case "INJUSTIFIEE" -> "badge-bad";
            default -> "";
        };
    }
}
