package com.gestionrh.sirh.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "demande_besoin")
public class DemandeBesoinModel {

    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter JOUR_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_demande_besoin")
    private Integer id;

    @Column(name = "numero", nullable = false, unique = true)
    private String numero;

    @Column(name = "id_demandeur", nullable = false)
    private Integer idDemandeur;

    @ManyToOne
    @JoinColumn(name = "id_departement", nullable = false)
    private DepartementModel departement;

    @ManyToOne
    @JoinColumn(name = "id_poste", nullable = false)
    private PosteModel poste;

    @ManyToOne
    @JoinColumn(name = "id_critere_recrutement")
    private CritereRecrutementModel critereRecrutement;

    @Column(name = "nombre_postes", nullable = false)
    private Integer nombrePostes = 1;

    @Column(name = "motif")
    private String motif;

    @Column(name = "niveau_urgence")
    private String niveauUrgence;

    @ManyToOne
    @JoinColumn(name = "id_type_contrat", nullable = false)
    private TypeContratModel typeContrat;

    @Column(name = "duree_mois")
    private Integer dureeMois;

    @Column(name = "salaire_propose")
    private BigDecimal salairePropose;

    @Column(name = "date_souhaitee")
    private LocalDate dateSouhaitee;

    @Column(name = "justification")
    private String justification;

    @Column(name = "statut", nullable = false)
    private String statut = "BROUILLON";

    @Transient
    private LocalDateTime dateCreation;

    @Transient
    private LocalDateTime dateSoumission;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getIdDemandeur() {
        return idDemandeur;
    }

    public void setIdDemandeur(Integer idDemandeur) {
        this.idDemandeur = idDemandeur;
    }

    public DepartementModel getDepartement() {
        return departement;
    }

    public void setDepartement(DepartementModel departement) {
        this.departement = departement;
    }

    public PosteModel getPoste() {
        return poste;
    }

    public void setPoste(PosteModel poste) {
        this.poste = poste;
    }

    public CritereRecrutementModel getCritereRecrutement() {
        return critereRecrutement;
    }

    public void setCritereRecrutement(CritereRecrutementModel critereRecrutement) {
        this.critereRecrutement = critereRecrutement;
    }

    public Integer getNombrePostes() {
        return nombrePostes;
    }

    public void setNombrePostes(Integer nombrePostes) {
        this.nombrePostes = nombrePostes;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }

    public String getNiveauUrgence() {
        return niveauUrgence;
    }

    public void setNiveauUrgence(String niveauUrgence) {
        this.niveauUrgence = niveauUrgence;
    }

    public TypeContratModel getTypeContrat() {
        return typeContrat;
    }

    public void setTypeContrat(TypeContratModel typeContrat) {
        this.typeContrat = typeContrat;
    }

    public Integer getDureeMois() {
        return dureeMois;
    }

    public void setDureeMois(Integer dureeMois) {
        this.dureeMois = dureeMois;
    }

    public BigDecimal getSalairePropose() {
        return salairePropose;
    }

    public void setSalairePropose(BigDecimal salairePropose) {
        this.salairePropose = salairePropose;
    }

    public LocalDate getDateSouhaitee() {
        return dateSouhaitee;
    }

    public void setDateSouhaitee(LocalDate dateSouhaitee) {
        this.dateSouhaitee = dateSouhaitee;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateSoumission() {
        return dateSoumission;
    }

    public void setDateSoumission(LocalDateTime dateSoumission) {
        this.dateSoumission = dateSoumission;
    }

    public boolean isModifiable() {
        return statut.equals("BROUILLON") || statut.equals("A_CORRIGER");
    }

    public boolean isRetirable() {
        return !statut.equals("VALIDEE") && !statut.equals("REJETEE") && !statut.equals("RETIREE");
    }

    public String getMotifLibelle() {
        if (motif == null) {
            return "-";
        }
        if (motif.equals("REMPLACEMENT")) {
            return "Remplacement";
        }
        if (motif.equals("RENFORT")) {
            return "PosteModel existant (renfort)";
        }
        if (motif.equals("CREATION")) {
            return "Création de poste";
        }
        return motif;
    }

    public String getUrgenceLibelle() {
        if (niveauUrgence == null) {
            return "-";
        }
        if (niveauUrgence.equals("CRITIQUE")) {
            return "Critique";
        }
        return "Urgent";
    }

    public String getStatutLibelle() {
        if (statut.equals("BROUILLON")) {
            return "Brouillon";
        }
        if (statut.equals("SOUMISE")) {
            return "Soumise";
        }
        if (statut.equals("EN_VERIFICATION")) {
            return "En vérification";
        }
        if (statut.equals("A_CORRIGER")) {
            return "À corriger";
        }
        if (statut.equals("VALIDEE")) {
            return "Validée";
        }
        if (statut.equals("REJETEE")) {
            return "Rejetée";
        }
        if (statut.equals("RETIREE")) {
            return "Retirée";
        }
        return statut;
    }

    public String getStatutBadge() {
        if (statut.equals("SOUMISE") || statut.equals("EN_VERIFICATION")) {
            return "badge-info";
        }
        if (statut.equals("A_CORRIGER")) {
            return "badge-warn";
        }
        if (statut.equals("VALIDEE")) {
            return "badge-ok";
        }
        if (statut.equals("REJETEE")) {
            return "badge-bad";
        }
        return "";
    }

    public String getDateSouhaiteeFr() {
        if (dateSouhaitee == null) {
            return "-";
        }
        return dateSouhaitee.format(JOUR);
    }

    public String getDateCreationFr() {
        if (dateCreation == null) {
            return "-";
        }
        return dateCreation.format(JOUR);
    }

    public String getDateSoumissionFr() {
        if (dateSoumission == null) {
            return "-";
        }
        return dateSoumission.format(JOUR_HEURE);
    }

    public String getSalaireFr() {
        if (salairePropose == null) {
            return "-";
        }
        DecimalFormatSymbols symboles = new DecimalFormatSymbols(Locale.FRANCE);
        symboles.setGroupingSeparator(' ');
        symboles.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0.##", symboles);
        return format.format(salairePropose) + " Ar";
    }
}
