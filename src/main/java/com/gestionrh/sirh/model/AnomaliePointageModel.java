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

/** Anomalie de pointage et sa régularisation (Bloc 6 §9). */
@Entity
@Table(name = "anomalie_pointage")
public class AnomaliePointageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_anomalie_pointage")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "jour", nullable = false)
    private LocalDate jour;

    @Column(name = "type_anomalie", nullable = false)
    private String typeAnomalie;

    @Column(name = "detail")
    private String detail;

    @Column(name = "statut", nullable = false)
    private String statut = "OUVERTE";

    @Column(name = "justification")
    private String justification;

    @Column(name = "heure_proposee")
    private LocalDateTime heureProposee;

    @Column(name = "sens_propose")
    private String sensPropose;

    @Column(name = "date_justification")
    private LocalDateTime dateJustification;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_traite_par")
    private EmployeModel traitePar;

    @Column(name = "date_traitement")
    private LocalDateTime dateTraitement;

    @Column(name = "commentaire_traitement")
    private String commentaireTraitement;

    @Column(name = "escaladee", nullable = false)
    private boolean escaladee;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = java.time.LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public LocalDate getJour() { return jour; }
    public void setJour(LocalDate jour) { this.jour = jour; }

    public String getTypeAnomalie() { return typeAnomalie; }
    public void setTypeAnomalie(String typeAnomalie) { this.typeAnomalie = typeAnomalie; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public LocalDateTime getHeureProposee() { return heureProposee; }
    public void setHeureProposee(LocalDateTime heureProposee) { this.heureProposee = heureProposee; }

    public String getSensPropose() { return sensPropose; }
    public void setSensPropose(String sensPropose) { this.sensPropose = sensPropose; }

    public LocalDateTime getDateJustification() { return dateJustification; }
    public void setDateJustification(LocalDateTime dateJustification) { this.dateJustification = dateJustification; }

    public EmployeModel getTraitePar() { return traitePar; }
    public void setTraitePar(EmployeModel traitePar) { this.traitePar = traitePar; }

    public LocalDateTime getDateTraitement() { return dateTraitement; }
    public void setDateTraitement(LocalDateTime dateTraitement) { this.dateTraitement = dateTraitement; }

    public String getCommentaireTraitement() { return commentaireTraitement; }
    public void setCommentaireTraitement(String commentaireTraitement) { this.commentaireTraitement = commentaireTraitement; }

    public boolean isEscaladee() { return escaladee; }
    public void setEscaladee(boolean escaladee) { this.escaladee = escaladee; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getJourFr() { return Formats.date(jour); }
    public boolean isATraiter() { return "OUVERTE".equals(statut) || "EN_COURS".equals(statut); }
    public String getHeureProposeeFr() { return heureProposee == null ? "" : Formats.dateHeure(heureProposee); }
    public String getTypeLibelle() {
        return switch (typeAnomalie) {
            case "POINTAGE_MANQUANT_ENTREE" -> "Entrée non pointée";
            case "POINTAGE_MANQUANT_SORTIE" -> "Sortie non pointée";
            case "ABSENCE_NON_JUSTIFIEE" -> "Absence non justifiée";
            case "RETARD" -> "Retard";
            case "DEPART_ANTICIPE" -> "Départ anticipé";
            case "HEURES_NON_AUTORISEES" -> "Heures non autorisées";
            case "POINTAGE_HORS_PLANNING" -> "Pointage hors planning";
            case "POINTAGE_INCOHERENT" -> "Pointages incohérents";
            default -> typeAnomalie;
        };
    }
    public String getStatutLibelle() {
        return switch (statut) {
            case "OUVERTE" -> "Ouverte";
            case "EN_COURS" -> "Justifiée, à traiter";
            case "RESOLUE" -> "Résolue";
            case "CONFIRMEE" -> "Confirmée";
            case "CLASSEE" -> "Classée sans suite";
            default -> statut;
        };
    }
    public String getStatutBadge() {
        return switch (statut) {
            case "OUVERTE" -> "badge-bad";
            case "EN_COURS" -> "badge-warn";
            case "RESOLUE" -> "badge-ok";
            default -> "";
        };
    }
}
