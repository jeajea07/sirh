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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "signature_contrat", uniqueConstraints =
        @UniqueConstraint(name = "uq_signature_partie", columnNames = {"id_contrat", "partie"}))
public class SignatureContratModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_signature_contrat")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat", nullable = false)
    private ContratModel contrat;

    @Column(name = "partie", nullable = false)
    private String partie;

    @Column(name = "nom_signataire", nullable = false)
    private String nomSignataire;

    @Column(name = "date_signature", nullable = false)
    private LocalDate dateSignature;

    @Column(name = "statut", nullable = false)
    private String statut = "SIGNEE";

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public ContratModel getContrat() { return contrat; }
    public void setContrat(ContratModel contrat) { this.contrat = contrat; }

    public String getPartie() { return partie; }
    public void setPartie(String partie) { this.partie = partie; }

    public String getNomSignataire() { return nomSignataire; }
    public void setNomSignataire(String nomSignataire) { this.nomSignataire = nomSignataire; }

    public LocalDate getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDate dateSignature) { this.dateSignature = dateSignature; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getPartieLibelle() {
        return "SALARIE".equals(partie) ? "Salarié" : "Entreprise";
    }

    public String getStatutLibelle() {
        if (statut == null) return "—";
        return switch (statut) {
            case "SIGNEE" -> "Signée";
            case "REFUSEE" -> "Refusée";
            default -> "Attendue";
        };
    }

    public String getStatutBadge() {
        if ("SIGNEE".equals(statut)) return "badge-ok";
        if ("REFUSEE".equals(statut)) return "badge-bad";
        return "badge-warn";
    }

    public String getDateSignatureFr() { return Formats.date(dateSignature); }
}
