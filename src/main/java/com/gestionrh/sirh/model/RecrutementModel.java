// Bloc 3
package com.gestionrh.sirh.model;

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
@Table(name = "b3_recrutement")
public class RecrutementModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recrutement")
    private Integer idRecrutement;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_selection", nullable = false, unique = true)
    private SelectionModel selection;
    @Column(name = "nombre_postes", nullable = false)
    private Integer nombrePostes;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_etat_recrutement", nullable = false)
    private EtatRecrutementModel etatRecrutement;
    @Column(name = "confirme_par")
    private Integer confirmePar;
    @Column(name = "confirme_le")
    private LocalDate confirmeLe;
    @Column(name = "motif_annulation")
    private String motifAnnulation;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_etat_transfert", nullable = false)
    private EtatTransfertModel etatTransfert;
    @Column(name = "transfert_le")
    private LocalDate transfertLe;
    @Column(name = "reference_bloc4")
    private String referenceBloc4;
    @Column(name = "erreur_transfert")
    private String erreurTransfert;

    public Integer getIdRecrutement() { return idRecrutement; }
    public SelectionModel getSelection() { return selection; }
    public void setSelection(SelectionModel v) { selection = v; }
    public Integer getNombrePostes() { return nombrePostes; }
    public void setNombrePostes(Integer v) { nombrePostes = v; }
    public EtatRecrutementModel getEtatRecrutement() { return etatRecrutement; }
    public void setEtatRecrutement(EtatRecrutementModel v) { etatRecrutement = v; }
    public Integer getConfirmePar() { return confirmePar; }
    public void setConfirmePar(Integer v) { confirmePar = v; }
    public LocalDate getConfirmeLe() { return confirmeLe; }
    public void setConfirmeLe(LocalDate v) { confirmeLe = v; }
    public String getMotifAnnulation() { return motifAnnulation; }
    public void setMotifAnnulation(String v) { motifAnnulation = v; }
    public EtatTransfertModel getEtatTransfert() { return etatTransfert; }
    public void setEtatTransfert(EtatTransfertModel v) { etatTransfert = v; }
    public LocalDate getTransfertLe() { return transfertLe; }
    public void setTransfertLe(LocalDate v) { transfertLe = v; }
    public String getReferenceBloc4() { return referenceBloc4; }
    public void setReferenceBloc4(String v) { referenceBloc4 = v; }
    public String getErreurTransfert() { return erreurTransfert; }
    public void setErreurTransfert(String v) { erreurTransfert = v; }
}
