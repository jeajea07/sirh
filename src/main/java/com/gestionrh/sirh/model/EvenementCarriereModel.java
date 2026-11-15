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
import org.hibernate.annotations.Immutable;

/**
 * Événement de carrière (Bloc 5 §11). En ajout seul : la base refuse UPDATE et DELETE (RG-HI02).
 */
@Entity
@Immutable
@Table(name = "evenement_carriere")
public class EvenementCarriereModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evenement_carriere")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "type_evenement", nullable = false)
    private String typeEvenement;

    @Column(name = "date_effet", nullable = false)
    private LocalDate dateEffet;

    @Column(name = "date_enregistrement", nullable = false)
    private LocalDateTime dateEnregistrement = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_auteur", nullable = false)
    private EmployeModel auteur;

    @Column(name = "libelle", nullable = false)
    private String libelle;

    @Column(name = "ancienne_valeur")
    private String ancienneValeur;

    @Column(name = "nouvelle_valeur")
    private String nouvelleValeur;

    @Column(name = "objet_source")
    private String objetSource;

    @Column(name = "id_objet_source")
    private Integer idObjetSource;

    public Integer getId() { return id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }
    public String getTypeEvenement() { return typeEvenement; }
    public void setTypeEvenement(String typeEvenement) { this.typeEvenement = typeEvenement; }
    public LocalDate getDateEffet() { return dateEffet; }
    public void setDateEffet(LocalDate dateEffet) { this.dateEffet = dateEffet; }
    public LocalDateTime getDateEnregistrement() { return dateEnregistrement; }
    public EmployeModel getAuteur() { return auteur; }
    public void setAuteur(EmployeModel auteur) { this.auteur = auteur; }
    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }
    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }
    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }
    public String getObjetSource() { return objetSource; }
    public void setObjetSource(String objetSource) { this.objetSource = objetSource; }
    public Integer getIdObjetSource() { return idObjetSource; }
    public void setIdObjetSource(Integer idObjetSource) { this.idObjetSource = idObjetSource; }

    public String getDateEffetFr() { return Formats.date(dateEffet); }
    public String getDateEnregistrementFr() { return Formats.dateHeure(dateEnregistrement); }
    public String getAuteurNom() { return auteur == null ? "—" : auteur.getNomComplet(); }

    public String getTypeLibelle() {
        if (typeEvenement == null) return "—";
        return switch (typeEvenement) {
            case "EMBAUCHE" -> "Embauche";
            case "REEMBAUCHE" -> "Réembauche";
            case "RENOUVELLEMENT_CONTRAT" -> "Renouvellement de contrat";
            case "TRANSFORMATION_CDI" -> "Passage en CDI";
            case "AVENANT" -> "Avenant";
            case "AFFECTATION" -> "Affectation";
            case "MUTATION" -> "Mutation";
            case "CHANGEMENT_POSTE" -> "Changement de poste";
            case "CHANGEMENT_RESPONSABLE" -> "Changement de responsable";
            case "PROMOTION" -> "Promotion";
            case "REVISION_SALAIRE" -> "Révision de salaire";
            case "SUSPENSION" -> "Suspension";
            case "REPRISE" -> "Reprise";
            case "SORTIE_ANNONCEE" -> "Sortie annoncée";
            case "SORTIE_ANNULEE" -> "Sortie annulée";
            case "SORTIE" -> "Sortie";
            case "CORRECTION" -> "Correction";
            default -> typeEvenement;
        };
    }

    public String getTypeBadge() {
        if (typeEvenement == null) return "badge-info";
        return switch (typeEvenement) {
            case "EMBAUCHE", "REEMBAUCHE", "TRANSFORMATION_CDI", "PROMOTION", "REPRISE" -> "badge-ok";
            case "SUSPENSION", "SORTIE_ANNONCEE" -> "badge-warn";
            case "SORTIE" -> "badge-bad";
            default -> "badge-info";
        };
    }
}
