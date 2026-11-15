package com.gestionrh.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

/** Bloc 2 — entretien d'un niveau (1 = RH, 2 = Technicien, 3 = DG) pour une candidature. */
@Entity
@Table(name = "b2_entretien")
public class EntretienModel {

    public static final String PLANIFIE = "PLANIFIE";
    public static final String REPORTE = "REPORTE";
    public static final String REALISE = "REALISE";
    public static final String ABSENT = "ABSENT";
    public static final String ADMIS = "ADMIS";
    public static final String NON_ADMIS = "NON_ADMIS";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entretien")
    private Integer id;

    @Column(name = "id_candidature", nullable = false)
    private Integer idCandidature;

    @Column(name = "niveau", nullable = false)
    private Short niveau;

    @Column(name = "date_prevue", nullable = false)
    private LocalDateTime datePrevue;

    @Column(name = "date_origine", nullable = false)
    private LocalDateTime dateOrigine;

    private String lieu;
    private String jury;

    @Column(nullable = false)
    private String statut = PLANIFIE;

    @Column(name = "motif_report")
    private String motifReport;

    @Column(name = "seuil_general", nullable = false)
    private BigDecimal seuilGeneral = new BigDecimal("3.00");

    @Column(name = "note_generale")
    private BigDecimal noteGenerale;

    @Column(name = "resultat_propose")
    private String resultatPropose;

    @Column(name = "motif_proposition")
    private String motifProposition;

    private String decision;

    @Column(name = "motif_decision")
    private String motifDecision;

    @Column(name = "id_decideur")
    private Integer idDecideur;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    @Column(name = "id_planificateur")
    private Integer idPlanificateur;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "entretien", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    private List<EntretienNoteModel> notes = new ArrayList<>();

    public static String libelleNiveau(int niveau) {
        return switch (niveau) {
            case 1 -> "Entretien 1 — RH";
            case 2 -> "Entretien 2 — Technicien";
            case 3 -> "Entretien 3 — DG";
            default -> "Entretien";
        };
    }

    private static final java.time.format.DateTimeFormatter FR =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String getDatePrevueFr() { return datePrevue == null ? "—" : datePrevue.format(FR); }
    public String getDateOrigineFr() { return dateOrigine == null ? "—" : dateOrigine.format(FR); }
    public String getDateDecisionFr() { return dateDecision == null ? "—" : dateDecision.format(FR); }
    public boolean isReporte() { return dateOrigine != null && datePrevue != null && !dateOrigine.equals(datePrevue); }
    public String getStatutLibelle() {
        return switch (statut) {
            case PLANIFIE -> "Planifié";
            case REPORTE -> "Reporté";
            case REALISE -> "Réalisé";
            case ABSENT -> "Absent";
            default -> statut;
        };
    }

    public String getNiveauLibelle() { return niveau == null ? "" : libelleNiveau(niveau); }
    public boolean isModifiable() { return PLANIFIE.equals(statut) || REPORTE.equals(statut); }
    public boolean isGrilleFigee() { return notes.stream().anyMatch(n -> n.getNote() != null) || !isModifiable(); }
    public boolean isDecide() { return decision != null; }
    public boolean isADecider() { return resultatPropose != null && decision == null; }

    public void ajouterNote(EntretienNoteModel n) {
        n.setEntretien(this);
        notes.add(n);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdCandidature() { return idCandidature; }
    public void setIdCandidature(Integer idCandidature) { this.idCandidature = idCandidature; }
    public Short getNiveau() { return niveau; }
    public void setNiveau(Short niveau) { this.niveau = niveau; }
    public LocalDateTime getDatePrevue() { return datePrevue; }
    public void setDatePrevue(LocalDateTime datePrevue) { this.datePrevue = datePrevue; }
    public LocalDateTime getDateOrigine() { return dateOrigine; }
    public void setDateOrigine(LocalDateTime dateOrigine) { this.dateOrigine = dateOrigine; }
    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }
    public String getJury() { return jury; }
    public void setJury(String jury) { this.jury = jury; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getMotifReport() { return motifReport; }
    public void setMotifReport(String motifReport) { this.motifReport = motifReport; }
    public BigDecimal getSeuilGeneral() { return seuilGeneral; }
    public void setSeuilGeneral(BigDecimal seuilGeneral) { this.seuilGeneral = seuilGeneral; }
    public BigDecimal getNoteGenerale() { return noteGenerale; }
    public void setNoteGenerale(BigDecimal noteGenerale) { this.noteGenerale = noteGenerale; }
    public String getResultatPropose() { return resultatPropose; }
    public void setResultatPropose(String resultatPropose) { this.resultatPropose = resultatPropose; }
    public String getMotifProposition() { return motifProposition; }
    public void setMotifProposition(String motifProposition) { this.motifProposition = motifProposition; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getMotifDecision() { return motifDecision; }
    public void setMotifDecision(String motifDecision) { this.motifDecision = motifDecision; }
    public Integer getIdDecideur() { return idDecideur; }
    public void setIdDecideur(Integer idDecideur) { this.idDecideur = idDecideur; }
    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }
    public Integer getIdPlanificateur() { return idPlanificateur; }
    public void setIdPlanificateur(Integer idPlanificateur) { this.idPlanificateur = idPlanificateur; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
    public List<EntretienNoteModel> getNotes() { return notes; }
    public void setNotes(List<EntretienNoteModel> notes) { this.notes = notes; }
}
