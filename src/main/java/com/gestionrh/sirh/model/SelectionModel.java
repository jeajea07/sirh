// Bloc 3
package com.gestionrh.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "b3_selection")
public class SelectionModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_selection")
    private Integer idSelection;

    @Column(name = "id_candidature_b2", nullable = false, unique = true)
    private Integer idCandidatureB2;
    @Column(name = "id_offre_b2", nullable = false)
    private Integer idOffreB2;
    @Column(name = "id_candidat_b2", nullable = false)
    private Integer idCandidatB2;
    @Column(name = "score_test")
    private BigDecimal scoreTest;
    @Column(name = "score_entretien_rh")
    private BigDecimal scoreEntretienRh;
    @Column(name = "score_entretien_tech")
    private BigDecimal scoreEntretienTech;
    @Column(name = "score_entretien_dg")
    private BigDecimal scoreEntretienDg;
    @Column(name = "score_final")
    private BigDecimal scoreFinal;
    @Column(name = "rang_classement")
    private Integer rangClassement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_etat_dossier", nullable = false)
    private EtatDossierModel etatDossier;
    @Column(name = "elements_manquants")
    private String elementsManquants;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_decision")
    private TypeDecisionModel decision;
    private String justification;
    @Column(name = "rang_liste_attente")
    private Integer rangListeAttente;
    @Column(name = "decide_par")
    private Integer decidePar;
    @Column(name = "decide_le")
    private LocalDate decideLe;

    @Column(name = "validation_obligatoire", nullable = false)
    private boolean validationObligatoire;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_etat_validation", nullable = false)
    private EtatValidationModel etatValidation;
    @Column(name = "valide_par")
    private Integer validePar;
    @Column(name = "valide_le")
    private LocalDate valideLe;

    public Integer getIdSelection() { return idSelection; }
    public Integer getIdCandidatureB2() { return idCandidatureB2; }
    public void setIdCandidatureB2(Integer v) { idCandidatureB2 = v; }
    public Integer getIdOffreB2() { return idOffreB2; }
    public void setIdOffreB2(Integer v) { idOffreB2 = v; }
    public Integer getIdCandidatB2() { return idCandidatB2; }
    public void setIdCandidatB2(Integer v) { idCandidatB2 = v; }
    public BigDecimal getScoreTest() { return scoreTest; }
    public void setScoreTest(BigDecimal v) { scoreTest = v; }
    public BigDecimal getScoreEntretienRh() { return scoreEntretienRh; }
    public void setScoreEntretienRh(BigDecimal v) { scoreEntretienRh = v; }
    public BigDecimal getScoreEntretienTech() { return scoreEntretienTech; }
    public void setScoreEntretienTech(BigDecimal v) { scoreEntretienTech = v; }
    public BigDecimal getScoreEntretienDg() { return scoreEntretienDg; }
    public void setScoreEntretienDg(BigDecimal v) { scoreEntretienDg = v; }
    public BigDecimal getScoreFinal() { return scoreFinal; }
    public void setScoreFinal(BigDecimal v) { scoreFinal = v; }
    public Integer getRangClassement() { return rangClassement; }
    public void setRangClassement(Integer v) { rangClassement = v; }
    public EtatDossierModel getEtatDossier() { return etatDossier; }
    public void setEtatDossier(EtatDossierModel v) { etatDossier = v; }
    public String getElementsManquants() { return elementsManquants; }
    public void setElementsManquants(String v) { elementsManquants = v; }
    public TypeDecisionModel getDecision() { return decision; }
    public void setDecision(TypeDecisionModel v) { decision = v; }
    public String getJustification() { return justification; }
    public void setJustification(String v) { justification = v; }
    public Integer getRangListeAttente() { return rangListeAttente; }
    public void setRangListeAttente(Integer v) { rangListeAttente = v; }
    public Integer getDecidePar() { return decidePar; }
    public void setDecidePar(Integer v) { decidePar = v; }
    public LocalDate getDecideLe() { return decideLe; }
    public void setDecideLe(LocalDate v) { decideLe = v; }
    public boolean isValidationObligatoire() { return validationObligatoire; }
    public void setValidationObligatoire(boolean v) { validationObligatoire = v; }
    public EtatValidationModel getEtatValidation() { return etatValidation; }
    public void setEtatValidation(EtatValidationModel v) { etatValidation = v; }
    public Integer getValidePar() { return validePar; }
    public void setValidePar(Integer v) { validePar = v; }
    public LocalDate getValideLe() { return valideLe; }
    public void setValideLe(LocalDate v) { valideLe = v; }
}
