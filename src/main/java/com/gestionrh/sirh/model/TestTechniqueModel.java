package com.gestionrh.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gestionrh.sirh.model.recrutement.Candidature;

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
    private String statut = "PROGRAMME";

    @Column(name = "seuil_note")
    private BigDecimal seuilNote = BigDecimal.ZERO;

    @Column(name = "note_generale")
    private BigDecimal noteGenerale = BigDecimal.ZERO;

    @Column(name = "relance")
    private boolean relance = false;

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
    public void setStatut(String statut) { this.statut = statut; }

    public BigDecimal getSeuilNote() { return seuilNote; }
    public void setSeuilNote(BigDecimal seuilNote) { this.seuilNote = seuilNote; }

    public BigDecimal getNoteGenerale() { return noteGenerale; }
    public void setNoteGenerale(BigDecimal noteGenerale) { this.noteGenerale = noteGenerale; }

    public boolean isRelance() { return relance; }
    public void setRelance(boolean relance) { this.relance = relance; }

    public List<ReponseTestTechniqueModel> getReponses() { return reponses; }
    public void setReponses(List<ReponseTestTechniqueModel> reponses) { this.reponses = reponses; }

    public void addReponse(ReponseTestTechniqueModel reponse) {
        reponses.add(reponse);
        reponse.setTestTechnique(this);
    }
}
