package com.gestionrh.sirh.model;

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
@Table(name = "validation_b4")
public class ValidationB4Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validation_b4")
    private Integer id;

    @Column(name = "type_objet", nullable = false)
    private String typeObjet;

    @Column(name = "id_objet", nullable = false)
    private Integer idObjet;

    @Column(name = "etape", nullable = false)
    private String etape;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validateur")
    private EmployeModel validateur;

    @Column(name = "decision")
    private String decision;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "date_reception", nullable = false)
    private LocalDateTime dateReception;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTypeObjet() { return typeObjet; }
    public void setTypeObjet(String typeObjet) { this.typeObjet = typeObjet; }

    public Integer getIdObjet() { return idObjet; }
    public void setIdObjet(Integer idObjet) { this.idObjet = idObjet; }

    public String getEtape() { return etape; }
    public void setEtape(String etape) { this.etape = etape; }

    public EmployeModel getValidateur() { return validateur; }
    public void setValidateur(EmployeModel validateur) { this.validateur = validateur; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public LocalDateTime getDateReception() { return dateReception; }
    public void setDateReception(LocalDateTime dateReception) { this.dateReception = dateReception; }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) { this.dateDecision = dateDecision; }

    public String getEtapeLibelle() {
        if (etape == null) return "—";
        return switch (etape) {
            case "VERIFICATION_RH" -> "Vérification RH";
            case "VALIDATION_RH" -> "Validation RH";
            case "CLOTURE" -> "Clôture";
            default -> etape;
        };
    }

    public String getDecisionLibelle() {
        if (decision == null) return "En attente";
        return switch (decision) {
            case "VALIDEE" -> "Validée";
            case "RENVOYEE" -> "Renvoyée";
            case "REJETEE" -> "Rejetée";
            default -> decision;
        };
    }

    public String getDecisionBadge() {
        if ("VALIDEE".equals(decision)) return "badge-ok";
        if ("REJETEE".equals(decision)) return "badge-bad";
        if ("RENVOYEE".equals(decision)) return "badge-warn";
        return "badge-info";
    }

    public String getDateReceptionFr() { return Formats.dateHeure(dateReception); }
    public String getDateDecisionFr() { return Formats.dateHeure(dateDecision); }
}
