package com.gestionrh.sirh.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(name = "declaration_sociale")
public class DeclarationSocialeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_declaration_sociale")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_organisme", nullable = false)
    private OrganismeModel organisme;

    @Column(name = "type_declaration", nullable = false)
    private String typeDeclaration;

    @Column(name = "date_preparation")
    private LocalDate datePreparation;

    @Column(name = "date_envoi")
    private LocalDate dateEnvoi;

    @Column(name = "reference_externe")
    private String referenceExterne;

    @Column(name = "statut", nullable = false)
    private String statut = "A_PREPARER";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_document_justificatif")
    private DocumentModel documentJustificatif;

    @Column(name = "commentaire")
    private String commentaire;

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

    public OrganismeModel getOrganisme() { return organisme; }
    public void setOrganisme(OrganismeModel organisme) { this.organisme = organisme; }

    public String getTypeDeclaration() { return typeDeclaration; }
    public void setTypeDeclaration(String typeDeclaration) { this.typeDeclaration = typeDeclaration; }

    public LocalDate getDatePreparation() { return datePreparation; }
    public void setDatePreparation(LocalDate datePreparation) { this.datePreparation = datePreparation; }

    public LocalDate getDateEnvoi() { return dateEnvoi; }
    public void setDateEnvoi(LocalDate dateEnvoi) { this.dateEnvoi = dateEnvoi; }

    public String getReferenceExterne() { return referenceExterne; }
    public void setReferenceExterne(String referenceExterne) { this.referenceExterne = referenceExterne; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public DocumentModel getDocumentJustificatif() { return documentJustificatif; }
    public void setDocumentJustificatif(DocumentModel documentJustificatif) { this.documentJustificatif = documentJustificatif; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public EmployeModel getCreePar() { return creePar; }
    public void setCreePar(EmployeModel creePar) { this.creePar = creePar; }

    public String getStatutLibelle() {
        if (statut == null) return "—";
        return switch (statut) {
            case "A_PREPARER" -> "À préparer";
            case "PREPAREE" -> "Préparée";
            case "TRANSMISE" -> "Transmise";
            case "CONFIRMEE" -> "Confirmée";
            case "REJETEE" -> "Rejetée";
            default -> statut;
        };
    }

    public String getStatutBadge() {
        if (statut == null) return "badge";
        return switch (statut) {
            case "CONFIRMEE" -> "badge-ok";
            case "REJETEE" -> "badge-bad";
            case "PREPAREE", "TRANSMISE" -> "badge-warn";
            default -> "badge-info";
        };
    }

    public String getDatePreparationFr() { return Formats.date(datePreparation); }
    public String getDateEnvoiFr() { return Formats.date(dateEnvoi); }
}
