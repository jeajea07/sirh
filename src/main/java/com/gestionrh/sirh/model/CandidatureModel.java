package com.gestionrh.sirh.model;

import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidature")
public class CandidatureModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_candidature")
    private Long idCandidature;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_candidat", nullable = false)
    private CandidatModel candidat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_offre")
    private OffreModel offre;

    @Column(name = "date_reception")
    private LocalDateTime dateDepot = LocalDateTime.now();

    @Column(name = "statut", length = 30)
    private String statut = "RECUE";

    @Column(name = "dossier_valide")
    private boolean dossierValide = false;

    @Column(name = "acces_cree")
    private boolean accesCree = false;

    @Column(name = "note_finale")
    private Double noteFinale;

    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TestTechniqueModel testTechnique;

    public Long getIdCandidature() {
        return idCandidature;
    }

    public void setIdCandidature(Long idCandidature) {
        this.idCandidature = idCandidature;
    }

    public CandidatModel getCandidat() {
        return candidat;
    }

    public void setCandidat(CandidatModel candidat) {
        this.candidat = candidat;
    }

    public OffreModel getOffre() {
        return offre;
    }

    public void setOffre(OffreModel offre) {
        this.offre = offre;
    }

    public LocalDateTime getDateDepot() {
        return dateDepot;
    }

    public void setDateDepot(LocalDateTime dateDepot) {
        this.dateDepot = dateDepot;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public boolean isDossierValide() {
        return dossierValide;
    }

    public void setDossierValide(boolean dossierValide) {
        this.dossierValide = dossierValide;
    }

    public boolean isAccesCree() {
        return accesCree;
    }

    public void setAccesCree(boolean accesCree) {
        this.accesCree = accesCree;
    }

    public Double getNoteFinale() {
        return noteFinale;
    }

    public void setNoteFinale(Double noteFinale) {
        this.noteFinale = noteFinale;
    }

    public TestTechniqueModel getTestTechnique() {
        return testTechnique;
    }

    public void setTestTechnique(TestTechniqueModel testTechnique) {
        this.testTechnique = testTechnique;
    }
}
