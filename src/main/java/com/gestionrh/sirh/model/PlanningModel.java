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

/** Planning d'un département sur une période (Bloc 6 §6). */
@Entity
@Table(name = "planning")
public class PlanningModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_planning")
    private Integer id;

    @Column(name = "reference", nullable = false)
    private String reference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_departement", nullable = false)
    private DepartementModel departement;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Column(name = "statut", nullable = false)
    private String statut = "BROUILLON";

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "id_planning_precedent")
    private Integer idPlanningPrecedent;

    @Column(name = "impact_budgetaire", nullable = false)
    private boolean impactBudgetaire;

    @Column(name = "cout_estime", nullable = false)
    private BigDecimal coutEstime = java.math.BigDecimal.ZERO;

    @Column(name = "motif_rejet")
    private String motifRejet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_auteur", nullable = false)
    private EmployeModel auteur;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = java.time.LocalDateTime.now();

    @Column(name = "date_soumission")
    private LocalDateTime dateSoumission;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_valideur_rh")
    private EmployeModel valideurRh;

    @Column(name = "date_validation_rh")
    private LocalDateTime dateValidationRh;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_valideur_finance")
    private EmployeModel valideurFinance;

    @Column(name = "date_validation_finance")
    private LocalDateTime dateValidationFinance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_decideur_dg")
    private EmployeModel decideurDg;

    @Column(name = "date_decision_dg")
    private LocalDateTime dateDecisionDg;

    @Column(name = "date_publication")
    private LocalDateTime datePublication;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public DepartementModel getDepartement() { return departement; }
    public void setDepartement(DepartementModel departement) { this.departement = departement; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public Integer getIdPlanningPrecedent() { return idPlanningPrecedent; }
    public void setIdPlanningPrecedent(Integer idPlanningPrecedent) { this.idPlanningPrecedent = idPlanningPrecedent; }

    public boolean isImpactBudgetaire() { return impactBudgetaire; }
    public void setImpactBudgetaire(boolean impactBudgetaire) { this.impactBudgetaire = impactBudgetaire; }

    public BigDecimal getCoutEstime() { return coutEstime; }
    public void setCoutEstime(BigDecimal coutEstime) { this.coutEstime = coutEstime; }

    public String getMotifRejet() { return motifRejet; }
    public void setMotifRejet(String motifRejet) { this.motifRejet = motifRejet; }

    public EmployeModel getAuteur() { return auteur; }
    public void setAuteur(EmployeModel auteur) { this.auteur = auteur; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateSoumission() { return dateSoumission; }
    public void setDateSoumission(LocalDateTime dateSoumission) { this.dateSoumission = dateSoumission; }

    public EmployeModel getValideurRh() { return valideurRh; }
    public void setValideurRh(EmployeModel valideurRh) { this.valideurRh = valideurRh; }

    public LocalDateTime getDateValidationRh() { return dateValidationRh; }
    public void setDateValidationRh(LocalDateTime dateValidationRh) { this.dateValidationRh = dateValidationRh; }

    public EmployeModel getValideurFinance() { return valideurFinance; }
    public void setValideurFinance(EmployeModel valideurFinance) { this.valideurFinance = valideurFinance; }

    public LocalDateTime getDateValidationFinance() { return dateValidationFinance; }
    public void setDateValidationFinance(LocalDateTime dateValidationFinance) { this.dateValidationFinance = dateValidationFinance; }

    public EmployeModel getDecideurDg() { return decideurDg; }
    public void setDecideurDg(EmployeModel decideurDg) { this.decideurDg = decideurDg; }

    public LocalDateTime getDateDecisionDg() { return dateDecisionDg; }
    public void setDateDecisionDg(LocalDateTime dateDecisionDg) { this.dateDecisionDg = dateDecisionDg; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public String getPeriodeLibelle() { return Formats.date(dateDebut) + " → " + Formats.date(dateFin); }
    public String getCoutFr() { return Formats.montant(coutEstime); }
    public boolean isModifiable() { return "BROUILLON".equals(statut) || "REJETE".equals(statut); }
    public String getStatutLibelle() {
        return switch (statut) {
            case "BROUILLON" -> "Brouillon";
            case "SOUMIS_RH" -> "À valider (RH)";
            case "SOUMIS_FINANCE" -> "À valider (Finance)";
            case "ARBITRAGE_DG" -> "Arbitrage DG";
            case "REJETE" -> "Rejeté";
            case "PUBLIE" -> "Publié";
            case "MODIFIE" -> "Remplacé";
            case "CLOTURE" -> "Clôturé";
            default -> statut;
        };
    }
    public String getStatutBadge() {
        return switch (statut) {
            case "PUBLIE" -> "badge-ok";
            case "REJETE" -> "badge-bad";
            case "SOUMIS_RH", "SOUMIS_FINANCE", "ARBITRAGE_DG" -> "badge-warn";
            case "BROUILLON" -> "badge-info";
            default -> "";
        };
    }
}
