package com.gestionrh.sirh.model;

import java.math.BigDecimal;
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

/** Une période de salaire de base (Bloc 5 §9). La ligne en cours a date_fin = null. */
@Entity
@Table(name = "historique_salaire")
public class HistoriqueSalaireModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique_salaire")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat")
    private ContratModel contrat;

    @Column(name = "salaire_base", nullable = false)
    private BigDecimal salaireBase;

    @Column(name = "categorie_professionnelle")
    private String categorieProfessionnelle;

    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "motif", nullable = false)
    private String motif;

    @Column(name = "commentaire")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "valide_par_id", nullable = false)
    private EmployeModel validePar;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Integer getId() { return id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }
    public ContratModel getContrat() { return contrat; }
    public void setContrat(ContratModel contrat) { this.contrat = contrat; }
    public BigDecimal getSalaireBase() { return salaireBase; }
    public void setSalaireBase(BigDecimal salaireBase) { this.salaireBase = salaireBase; }
    public String getCategorieProfessionnelle() { return categorieProfessionnelle; }
    public void setCategorieProfessionnelle(String categorieProfessionnelle) { this.categorieProfessionnelle = categorieProfessionnelle; }
    public LocalDate getDateEffet() { return dateEffet; }
    public void setDateEffet(LocalDate dateEffet) { this.dateEffet = dateEffet; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public EmployeModel getValidePar() { return validePar; }
    public void setValidePar(EmployeModel validePar) { this.validePar = validePar; }
    public LocalDateTime getDateCreation() { return dateCreation; }

    public String getSalaireFr() { return Formats.montant(salaireBase); }
    public String getDateEffetFr() { return Formats.date(dateEffet); }
    public String getDateFinFr() { return dateFin == null ? "En cours" : Formats.date(dateFin); }
    public boolean isEnCours() { return dateFin == null; }

    public String getMotifLibelle() {
        if (motif == null) return "—";
        return switch (motif) {
            case "EMBAUCHE" -> "Embauche";
            case "RENOUVELLEMENT" -> "Renouvellement";
            case "AVENANT" -> "Avenant";
            case "AUGMENTATION_GENERALE" -> "Augmentation générale";
            case "AUGMENTATION_INDIVIDUELLE" -> "Augmentation individuelle";
            case "PROMOTION" -> "Promotion";
            case "CORRECTION" -> "Correction";
            default -> motif;
        };
    }
}
