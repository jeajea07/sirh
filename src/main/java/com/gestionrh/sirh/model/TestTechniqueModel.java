package com.gestionrh.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.service.Bloc2BStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Session de test technique rattachee a une candidature du Bloc 2A.
 * Le statut reste une chaine : les etapes du test ne sont pas celles de CandidatureStatut.
 */
@Entity
@Table(name = "test_technique")
public class TestTechniqueModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_test_technique")
    private Integer idTestTechnique;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidature", nullable = false, unique = true)
    private Candidature candidature;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut = LocalDateTime.now();

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Column(name = "statut")
    private String statut = Bloc2BStatus.TEST_PROGRAMME;

    @Column(name = "seuil_note")
    private BigDecimal seuilNote = BigDecimal.ZERO;

    @Column(name = "note_generale")
    private BigDecimal noteGenerale = BigDecimal.ZERO;

    @Column(name = "relance")
    private boolean relance = false;

    @Column(name = "absent")
    private boolean absent = false;

    @Column(name = "correction_manuelle")
    private boolean correctionManuelle = false;

    @Column(name = "motif_absence")
    private String motifAbsence;

    @Column(name = "motif_exception")
    private String motifException;

    @Column(name = "duree_secondes")
    private Integer dureeSecondes = 1800;

    @OneToMany(mappedBy = "testTechnique", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReponseTestTechniqueModel> reponses = new ArrayList<>();

    public Integer getIdTestTechnique() { return idTestTechnique; }
    public void setIdTestTechnique(Integer idTestTechnique) { this.idTestTechnique = idTestTechnique; }

    public Candidature getCandidature() { return candidature; }
    public void setCandidature(Candidature candidature) { this.candidature = candidature; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateFin() { return dateFin; }
    public void setDateFin(LocalDateTime dateFin) { this.dateFin = dateFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut == null ? Bloc2BStatus.TEST_PROGRAMME : statut; }

    public BigDecimal getSeuilNote() { return seuilNote; }
    public void setSeuilNote(BigDecimal seuilNote) { this.seuilNote = seuilNote; }

    public BigDecimal getNoteGenerale() { return noteGenerale; }
    public void setNoteGenerale(BigDecimal noteGenerale) { this.noteGenerale = noteGenerale; }

    public boolean isRelance() { return relance; }
    public void setRelance(boolean relance) { this.relance = relance; }

    public boolean isAbsent() { return absent || Bloc2BStatus.TEST_ABSENT.equalsIgnoreCase(statut); }
    public void setAbsent(boolean absent) {
        this.absent = absent;
        if (absent) {
            this.statut = Bloc2BStatus.TEST_ABSENT;
        }
    }

    public boolean isCorrectionManuelle() { return correctionManuelle || Bloc2BStatus.TEST_CORRIGE.equalsIgnoreCase(statut); }
    public void setCorrectionManuelle(boolean correctionManuelle) {
        this.correctionManuelle = correctionManuelle;
        if (correctionManuelle) {
            this.statut = Bloc2BStatus.TEST_CORRIGE;
        }
    }

    public String getMotifAbsence() { return motifAbsence; }
    public void setMotifAbsence(String motifAbsence) { this.motifAbsence = motifAbsence; }

    public String getMotifException() { return motifException; }
    public void setMotifException(String motifException) { this.motifException = motifException; }

    public Integer getDureeSecondes() { return dureeSecondes; }
    public void setDureeSecondes(Integer dureeSecondes) { this.dureeSecondes = dureeSecondes; }

    public List<ReponseTestTechniqueModel> getReponses() { return reponses; }
    public void setReponses(List<ReponseTestTechniqueModel> reponses) { this.reponses = reponses; }

    public void addReponse(ReponseTestTechniqueModel reponse) {
        reponses.add(reponse);
        reponse.setTestTechnique(this);
    }

    public void marquerAbsence(String motif) {
        this.absent = true;
        this.motifAbsence = motif;
        this.statut = Bloc2BStatus.TEST_ABSENT;
    }

    public void relancer() {
        this.relance = true;
        this.statut = Bloc2BStatus.TEST_RELANCE;
    }

    public void corrigerManuellement() {
        this.correctionManuelle = true;
        this.statut = Bloc2BStatus.TEST_CORRIGE;
    }

    public boolean estExpiree() {
        LocalDateTime fin = dateFin != null ? dateFin : (dateDebut != null && dureeSecondes != null ? dateDebut.plusSeconds(dureeSecondes) : null);
        return fin != null && LocalDateTime.now().isAfter(fin);
    }

    public long getTempsRestantSecondes() {
        LocalDateTime fin = dateFin != null ? dateFin : (dateDebut != null && dureeSecondes != null ? dateDebut.plusSeconds(dureeSecondes) : null);
        if (fin == null) {
            return 0L;
        }
        return Math.max(0L, ChronoUnit.SECONDS.between(LocalDateTime.now(), fin));
    }
}
