package com.gestionrh.sirh.model;

import com.gestionrh.sirh.util.Formats;
import java.math.BigDecimal;
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

/** Demande de congé et son circuit de validation (Bloc 7 §7). */
@Entity
@Table(name = "demande_conge")
public class DemandeCongeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande_conge")
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_conge", nullable = false)
    private TypeCongeModel type;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "demi_journee_debut", nullable = false)
    private String demiJourneeDebut = "MATIN";

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "demi_journee_fin", nullable = false)
    private String demiJourneeFin = "APRES_MIDI";

    @Column(name = "nb_jours", nullable = false)
    private BigDecimal nbJours;

    @Column(name = "jours_pris")
    private BigDecimal joursPris;

    @Column(name = "motif")
    private String motif;

    @Column(name = "id_justificatif")
    private Integer idJustificatif;

    @Column(name = "statut", nullable = false)
    private String statut = "SOUMISE";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_manager")
    private EmployeModel manager;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_valideur_manager")
    private EmployeModel valideurManager;

    @Column(name = "date_validation_manager")
    private LocalDateTime dateValidationManager;

    @Column(name = "commentaire_manager")
    private String commentaireManager;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_valideur_rh")
    private EmployeModel valideurRh;

    @Column(name = "date_validation_rh")
    private LocalDateTime dateValidationRh;

    @Column(name = "commentaire_rh")
    private String commentaireRh;

    @Column(name = "motif_refus")
    private String motifRefus;

    @Column(name = "motif_annulation")
    private String motifAnnulation;

    @Column(name = "escaladee", nullable = false)
    private boolean escaladee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_saisi_par", nullable = false)
    private EmployeModel saisiPar;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = java.time.LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public TypeCongeModel getType() { return type; }
    public void setType(TypeCongeModel type) { this.type = type; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public String getDemiJourneeDebut() { return demiJourneeDebut; }
    public void setDemiJourneeDebut(String demiJourneeDebut) { this.demiJourneeDebut = demiJourneeDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getDemiJourneeFin() { return demiJourneeFin; }
    public void setDemiJourneeFin(String demiJourneeFin) { this.demiJourneeFin = demiJourneeFin; }

    public BigDecimal getNbJours() { return nbJours; }
    public void setNbJours(BigDecimal nbJours) { this.nbJours = nbJours; }

    public BigDecimal getJoursPris() { return joursPris; }
    public void setJoursPris(BigDecimal joursPris) { this.joursPris = joursPris; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public Integer getIdJustificatif() { return idJustificatif; }
    public void setIdJustificatif(Integer idJustificatif) { this.idJustificatif = idJustificatif; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public EmployeModel getManager() { return manager; }
    public void setManager(EmployeModel manager) { this.manager = manager; }

    public EmployeModel getValideurManager() { return valideurManager; }
    public void setValideurManager(EmployeModel valideurManager) { this.valideurManager = valideurManager; }

    public LocalDateTime getDateValidationManager() { return dateValidationManager; }
    public void setDateValidationManager(LocalDateTime dateValidationManager) { this.dateValidationManager = dateValidationManager; }

    public String getCommentaireManager() { return commentaireManager; }
    public void setCommentaireManager(String commentaireManager) { this.commentaireManager = commentaireManager; }

    public EmployeModel getValideurRh() { return valideurRh; }
    public void setValideurRh(EmployeModel valideurRh) { this.valideurRh = valideurRh; }

    public LocalDateTime getDateValidationRh() { return dateValidationRh; }
    public void setDateValidationRh(LocalDateTime dateValidationRh) { this.dateValidationRh = dateValidationRh; }

    public String getCommentaireRh() { return commentaireRh; }
    public void setCommentaireRh(String commentaireRh) { this.commentaireRh = commentaireRh; }

    public String getMotifRefus() { return motifRefus; }
    public void setMotifRefus(String motifRefus) { this.motifRefus = motifRefus; }

    public String getMotifAnnulation() { return motifAnnulation; }
    public void setMotifAnnulation(String motifAnnulation) { this.motifAnnulation = motifAnnulation; }

    public boolean isEscaladee() { return escaladee; }
    public void setEscaladee(boolean escaladee) { this.escaladee = escaladee; }

    public EmployeModel getSaisiPar() { return saisiPar; }
    public void setSaisiPar(EmployeModel saisiPar) { this.saisiPar = saisiPar; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    /** Statuts qui réservent ou consomment des jours (RG-SC05, RG-CH01). */
    public static final java.util.List<String> ACTIFS =
            java.util.List.of("SOUMISE", "VALIDEE_MANAGER", "VALIDEE", "EN_COURS", "PRISE", "INTERROMPUE");

    public String getPeriodeLibelle() {
        return Formats.date(dateDebut) + ("APRES_MIDI".equals(demiJourneeDebut) ? " (après-midi)" : "")
                + " → " + Formats.date(dateFin) + ("MATIN".equals(demiJourneeFin) ? " (matin)" : "");
    }
    public String getNbJoursFr() { return nbJours.stripTrailingZeros().toPlainString() + " j"; }
    public boolean isAnnulableParSalarie() {
        return "SOUMISE".equals(statut) || "VALIDEE_MANAGER".equals(statut)
                || ("VALIDEE".equals(statut) && dateDebut.isAfter(java.time.LocalDate.now()));
    }
    public String getStatutLibelle() {
        return switch (statut) {
            case "BROUILLON" -> "Brouillon";
            case "SOUMISE" -> "En attente du manager";
            case "VALIDEE_MANAGER" -> "En attente du RH";
            case "VALIDEE" -> "Validée";
            case "REFUSEE" -> "Refusée";
            case "ANNULEE" -> "Annulée";
            case "EN_COURS" -> "En cours";
            case "PRISE" -> "Prise";
            case "INTERROMPUE" -> "Interrompue";
            default -> statut;
        };
    }
    public String getStatutBadge() {
        return switch (statut) {
            case "VALIDEE", "EN_COURS", "PRISE" -> "badge-ok";
            case "SOUMISE", "VALIDEE_MANAGER" -> "badge-warn";
            case "REFUSEE" -> "badge-bad";
            default -> "";
        };
    }
}
