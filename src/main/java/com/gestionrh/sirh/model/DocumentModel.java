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
@Table(name = "document")
public class DocumentModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_document")
    private Integer id;

    @Column(name = "type_document", nullable = false)
    private String typeDocument;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    @Column(name = "chemin_stockage", nullable = false)
    private String cheminStockage;

    @Column(name = "type_mime")
    private String typeMime;

    @Column(name = "taille_octets")
    private Long tailleOctets;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "statut_validation", nullable = false)
    private String statutValidation = "RECU";

    @Column(name = "niveau_confidentialite", nullable = false)
    private String niveauConfidentialite = "INTERNE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe")
    private EmployeModel employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat")
    private ContratModel contrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_onboarding_tache")
    private OnboardingTacheModel onboardingTache;

    @Column(name = "date_depot", nullable = false)
    private LocalDateTime dateDepot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depose_par_id", nullable = false)
    private EmployeModel deposePar;

    @Column(name = "date_verification")
    private LocalDateTime dateVerification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verifie_par_id")
    private EmployeModel verifiePar;

    @Column(name = "motif_rejet")
    private String motifRejet;

    @Column(name = "commentaire")
    private String commentaire;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTypeDocument() { return typeDocument; }
    public void setTypeDocument(String typeDocument) { this.typeDocument = typeDocument; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getCheminStockage() { return cheminStockage; }
    public void setCheminStockage(String cheminStockage) { this.cheminStockage = cheminStockage; }

    public String getTypeMime() { return typeMime; }
    public void setTypeMime(String typeMime) { this.typeMime = typeMime; }

    public Long getTailleOctets() { return tailleOctets; }
    public void setTailleOctets(Long tailleOctets) { this.tailleOctets = tailleOctets; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getStatutValidation() { return statutValidation; }
    public void setStatutValidation(String statutValidation) { this.statutValidation = statutValidation; }

    public String getNiveauConfidentialite() { return niveauConfidentialite; }
    public void setNiveauConfidentialite(String niveauConfidentialite) { this.niveauConfidentialite = niveauConfidentialite; }

    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public ContratModel getContrat() { return contrat; }
    public void setContrat(ContratModel contrat) { this.contrat = contrat; }

    public OnboardingTacheModel getOnboardingTache() { return onboardingTache; }
    public void setOnboardingTache(OnboardingTacheModel onboardingTache) { this.onboardingTache = onboardingTache; }

    public LocalDateTime getDateDepot() { return dateDepot; }
    public void setDateDepot(LocalDateTime dateDepot) { this.dateDepot = dateDepot; }

    public EmployeModel getDeposePar() { return deposePar; }
    public void setDeposePar(EmployeModel deposePar) { this.deposePar = deposePar; }

    public LocalDateTime getDateVerification() { return dateVerification; }
    public void setDateVerification(LocalDateTime dateVerification) { this.dateVerification = dateVerification; }

    public EmployeModel getVerifiePar() { return verifiePar; }
    public void setVerifiePar(EmployeModel verifiePar) { this.verifiePar = verifiePar; }

    public String getMotifRejet() { return motifRejet; }
    public void setMotifRejet(String motifRejet) { this.motifRejet = motifRejet; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getTypeLibelle() {
        if (typeDocument == null) return "—";
        return switch (typeDocument) {
            case "CONTRAT" -> "Contrat";
            case "ADMINISTRATIF" -> "Administratif";
            case "SOCIAL" -> "Social";
            case "SANTE" -> "Santé";
            case "INTERNE" -> "Interne";
            case "FORMATION" -> "Formation";
            case "MATERIEL" -> "Matériel";
            case "CV" -> "CV";
            default -> typeDocument;
        };
    }

    public String getStatutLibelle() {
        if (statutValidation == null) return "—";
        return switch (statutValidation) {
            case "RECU" -> "Reçu";
            case "VERIFIE" -> "Vérifié";
            case "REJETE" -> "Rejeté";
            default -> "Attendu";
        };
    }

    public String getStatutBadge() {
        if ("VERIFIE".equals(statutValidation)) return "badge-ok";
        if ("REJETE".equals(statutValidation)) return "badge-bad";
        if ("ATTENDU".equals(statutValidation)) return "badge-warn";
        return "badge-info";
    }

    public String getConfidentialiteLibelle() {
        if (niveauConfidentialite == null) return "—";
        return switch (niveauConfidentialite) {
            case "PUBLIC" -> "Public";
            case "CONFIDENTIEL" -> "Confidentiel";
            case "SECRET" -> "Secret";
            default -> "Interne";
        };
    }

    public String getConfidentialiteBadge() {
        if ("SECRET".equals(niveauConfidentialite)) return "badge-bad";
        if ("CONFIDENTIEL".equals(niveauConfidentialite)) return "badge-warn";
        if ("PUBLIC".equals(niveauConfidentialite)) return "badge-ok";
        return "badge-info";
    }

    public String getTailleFr() { return Formats.taille(tailleOctets); }
    public String getDateDepotFr() { return Formats.dateHeure(dateDepot); }
}
