package com.gestionrh.sirh.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.gestionrh.sirh.util.Formats;

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
@Table(name = "contrat")
public class ContratModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrat")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_contrat", nullable = false)
    private TypeContratModel typeContrat;

    @Column(name = "type_operation", nullable = false)
    private String typeOperation = "INITIAL";

    @Column(name = "date_signature")
    private LocalDate dateSignature;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "fonction", nullable = false)
    private String fonction;

    @Column(name = "categorie_professionnelle")
    private String categorieProfessionnelle;

    @Column(name = "salaire", nullable = false)
    private BigDecimal salaire;

    @Column(name = "statut_workflow", nullable = false)
    private String statutWorkflow = "BROUILLON";

    @Column(name = "statut_contractuel", nullable = false)
    private String statutContractuel = "NON_ACTIF";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrat_precedent_id")
    private ContratModel contratPrecedent;

    @Column(name = "id_recrutement_externe", unique = true)
    private Integer idRecrutementExterne;

    @Column(name = "motif_recours")
    private String motifRecours;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id", nullable = false)
    private EmployeModel creePar;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public TypeContratModel getTypeContrat() { return typeContrat; }
    public void setTypeContrat(TypeContratModel typeContrat) { this.typeContrat = typeContrat; }

    public String getTypeOperation() { return typeOperation; }
    public void setTypeOperation(String typeOperation) { this.typeOperation = typeOperation; }

    public LocalDate getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDate dateSignature) { this.dateSignature = dateSignature; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getFonction() { return fonction; }
    public void setFonction(String fonction) { this.fonction = fonction; }

    public String getCategorieProfessionnelle() { return categorieProfessionnelle; }
    public void setCategorieProfessionnelle(String categorieProfessionnelle) { this.categorieProfessionnelle = categorieProfessionnelle; }

    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }

    public String getStatutWorkflow() { return statutWorkflow; }
    public void setStatutWorkflow(String statutWorkflow) { this.statutWorkflow = statutWorkflow; }

    public String getStatutContractuel() { return statutContractuel; }
    public void setStatutContractuel(String statutContractuel) { this.statutContractuel = statutContractuel; }

    public ContratModel getContratPrecedent() { return contratPrecedent; }
    public void setContratPrecedent(ContratModel contratPrecedent) { this.contratPrecedent = contratPrecedent; }

    public Integer getIdRecrutementExterne() { return idRecrutementExterne; }
    public void setIdRecrutementExterne(Integer idRecrutementExterne) { this.idRecrutementExterne = idRecrutementExterne; }

    public String getMotifRecours() { return motifRecours; }
    public void setMotifRecours(String motifRecours) { this.motifRecours = motifRecours; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public EmployeModel getCreePar() { return creePar; }
    public void setCreePar(EmployeModel creePar) { this.creePar = creePar; }

    public boolean isModifiable() { return "BROUILLON".equals(statutWorkflow); }
    public boolean isSoumettable() { return "BROUILLON".equals(statutWorkflow); }
    public boolean isSignable() { return "A_SIGNER".equals(statutWorkflow); }
    public boolean isActivable() {
        return "SIGNE".equals(statutWorkflow) && dateDebut != null && !dateDebut.isAfter(LocalDate.now());
    }
    public boolean isTerminable() { return "EN_VIGUEUR".equals(statutWorkflow); }

    public String getStatutLibelle() {
        if (statutWorkflow == null) return "—";
        return switch (statutWorkflow) {
            case "BROUILLON" -> "Brouillon";
            case "A_VALIDER" -> "À valider";
            case "A_SIGNER" -> "À signer";
            case "SIGNE" -> "Signé";
            case "EN_VIGUEUR" -> "En vigueur";
            case "TERMINE" -> "Terminé";
            case "ANNULE" -> "Annulé";
            default -> statutWorkflow;
        };
    }

    public String getStatutBadge() {
        if (statutWorkflow == null) return "badge";
        return switch (statutWorkflow) {
            case "EN_VIGUEUR", "SIGNE" -> "badge-ok";
            case "A_VALIDER", "A_SIGNER" -> "badge-warn";
            case "TERMINE", "ANNULE" -> "badge-bad";
            default -> "badge-info";
        };
    }

    public String getStatutContractuelLibelle() {
        if (statutContractuel == null) return "—";
        return switch (statutContractuel) {
            case "ACTIF" -> "Actif";
            case "REMPLACE" -> "Remplacé";
            case "SUSPENDU" -> "Suspendu";
            case "TERMINE" -> "Terminé";
            default -> "Non actif";
        };
    }

    public String getTypeOperationLibelle() {
        if (typeOperation == null) return "—";
        return switch (typeOperation) {
            case "INITIAL" -> "Création";
            case "RENOUVELLEMENT" -> "Renouvellement";
            case "AVENANT" -> "Avenant";
            default -> typeOperation;
        };
    }

    public String getSalaireFr() { return Formats.montant(salaire); }
    public String getDateDebutFr() { return Formats.date(dateDebut); }
    public String getDateFinFr() { return Formats.date(dateFin); }
    public String getDateSignatureFr() { return Formats.date(dateSignature); }

    public long getJoursRestants() { return Formats.joursRestants(dateFin); }

    public Integer getDureeMois() {
        if (dateDebut == null || dateFin == null) return null;
        long mois = ChronoUnit.MONTHS.between(dateDebut, dateFin);
        return (int) Math.max(mois, 1);
    }
}
