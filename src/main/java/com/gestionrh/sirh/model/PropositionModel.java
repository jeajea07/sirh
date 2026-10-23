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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "b3_proposition", uniqueConstraints =
        @UniqueConstraint(name = "uq_b3_proposition_version", columnNames = {"id_selection", "numero_version"}))
public class PropositionModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proposition")
    private Integer idProposition;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_selection", nullable = false)
    private SelectionModel selection;
    @Column(name = "numero_version", nullable = false)
    private Integer numeroVersion;
    @Column(name = "intitule_poste", nullable = false)
    private String intitulePoste;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_type_contrat", nullable = false)
    private TypeContratEmbaucheModel typeContrat;
    @Column(name = "salaire_mensuel", nullable = false)
    private BigDecimal salaireMensuel;
    @Column(name = "date_debut_prevue", nullable = false)
    private LocalDate dateDebutPrevue;
    @Column(name = "duree_mois")
    private Integer dureeMois;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_etat_proposition", nullable = false)
    private EtatPropositionModel etatProposition;
    @Column(name = "cree_par", nullable = false)
    private Integer creePar;
    @Column(name = "cree_le", nullable = false)
    private LocalDate creeLe;
    @Column(name = "envoyee_le")
    private LocalDate envoyeeLe;
    @Column(name = "date_limite_reponse")
    private LocalDate dateLimiteReponse;

    public Integer getIdProposition() { return idProposition; }
    public SelectionModel getSelection() { return selection; }
    public void setSelection(SelectionModel v) { selection = v; }
    public Integer getNumeroVersion() { return numeroVersion; }
    public void setNumeroVersion(Integer v) { numeroVersion = v; }
    public String getIntitulePoste() { return intitulePoste; }
    public void setIntitulePoste(String v) { intitulePoste = v; }
    public TypeContratEmbaucheModel getTypeContrat() { return typeContrat; }
    public void setTypeContrat(TypeContratEmbaucheModel v) { typeContrat = v; }
    public BigDecimal getSalaireMensuel() { return salaireMensuel; }
    public void setSalaireMensuel(BigDecimal v) { salaireMensuel = v; }
    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public void setDateDebutPrevue(LocalDate v) { dateDebutPrevue = v; }
    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer v) { dureeMois = v; }
    public EtatPropositionModel getEtatProposition() { return etatProposition; }
    public void setEtatProposition(EtatPropositionModel v) { etatProposition = v; }
    public Integer getCreePar() { return creePar; }
    public void setCreePar(Integer v) { creePar = v; }
    public LocalDate getCreeLe() { return creeLe; }
    public void setCreeLe(LocalDate v) { creeLe = v; }
    public LocalDate getEnvoyeeLe() { return envoyeeLe; }
    public void setEnvoyeeLe(LocalDate v) { envoyeeLe = v; }
    public LocalDate getDateLimiteReponse() { return dateLimiteReponse; }
    public void setDateLimiteReponse(LocalDate v) { dateLimiteReponse = v; }
}
