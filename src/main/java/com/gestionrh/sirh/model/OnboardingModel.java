package com.gestionrh.sirh.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.gestionrh.sirh.util.Formats;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "onboarding")
public class OnboardingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_onboarding")
    private Integer id;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat", nullable = false)
    private ContratModel contrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modele_onboarding", nullable = false)
    private ModeleOnboardingModel modele;

    @Column(name = "version_modele", nullable = false)
    private Integer versionModele;

    @Column(name = "date_debut_prevue", nullable = false)
    private LocalDate dateDebutPrevue;

    @Column(name = "date_debut_reelle")
    private LocalDate dateDebutReelle;

    @Column(name = "date_fin_prevue")
    private LocalDate dateFinPrevue;

    @Column(name = "date_cloture")
    private LocalDateTime dateCloture;

    @Column(name = "statut", nullable = false)
    private String statut = "A_PREPARER";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable_rh", nullable = false)
    private EmployeModel responsableRh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cloture_par")
    private EmployeModel cloturePar;

    @Column(name = "motif_annulation")
    private String motifAnnulation;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id", nullable = false)
    private EmployeModel creePar;

    @OneToMany(mappedBy = "onboarding", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC, id ASC")
    private List<OnboardingTacheModel> taches = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public ContratModel getContrat() { return contrat; }
    public void setContrat(ContratModel contrat) { this.contrat = contrat; }

    public ModeleOnboardingModel getModele() { return modele; }
    public void setModele(ModeleOnboardingModel modele) { this.modele = modele; }

    public Integer getVersionModele() { return versionModele; }
    public void setVersionModele(Integer versionModele) { this.versionModele = versionModele; }

    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public void setDateDebutPrevue(LocalDate dateDebutPrevue) { this.dateDebutPrevue = dateDebutPrevue; }

    public LocalDate getDateDebutReelle() { return dateDebutReelle; }
    public void setDateDebutReelle(LocalDate dateDebutReelle) { this.dateDebutReelle = dateDebutReelle; }

    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }

    public LocalDateTime getDateCloture() { return dateCloture; }
    public void setDateCloture(LocalDateTime dateCloture) { this.dateCloture = dateCloture; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public EmployeModel getResponsableRh() { return responsableRh; }
    public void setResponsableRh(EmployeModel responsableRh) { this.responsableRh = responsableRh; }

    public EmployeModel getCloturePar() { return cloturePar; }
    public void setCloturePar(EmployeModel cloturePar) { this.cloturePar = cloturePar; }

    public String getMotifAnnulation() { return motifAnnulation; }
    public void setMotifAnnulation(String motifAnnulation) { this.motifAnnulation = motifAnnulation; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public EmployeModel getCreePar() { return creePar; }
    public void setCreePar(EmployeModel creePar) { this.creePar = creePar; }

    public List<OnboardingTacheModel> getTaches() { return taches; }
    public void setTaches(List<OnboardingTacheModel> taches) { this.taches = taches; }

    public boolean isCloture() { return "CLOTURE".equals(statut); }
    public boolean isAnnule() { return "ANNULE".equals(statut); }

    public int getProgression() {
        List<OnboardingTacheModel> actives = taches == null ? List.of() : taches.stream()
                .filter(t -> !"ANNULEE".equals(t.getStatut())).toList();
        if (actives.isEmpty()) return 100;
        long terminees = actives.stream().filter(OnboardingTacheModel::isTerminee).count();
        return (int) (terminees * 100 / actives.size());
    }

    public int getTachesTerminees() {
        return taches == null ? 0 : (int) taches.stream().filter(OnboardingTacheModel::isTerminee).count();
    }

    public int getTachesEnRetard() {
        return taches == null ? 0 : (int) taches.stream().filter(OnboardingTacheModel::isEnRetard).count();
    }

    public int getTachesBloquees() {
        return taches == null ? 0 : (int) taches.stream().filter(OnboardingTacheModel::isBloquee).count();
    }

    public boolean isCloturable() {
        return taches != null && taches.stream()
                .filter(OnboardingTacheModel::isObligatoire)
                .allMatch(OnboardingTacheModel::isTerminee);
    }

    public List<OnboardingTacheModel> getTachesObligatoiresRestantes() {
        if (taches == null) return List.of();
        return taches.stream().filter(t -> t.isObligatoire() && !t.isTerminee()).toList();
    }

    public List<OnboardingTacheModel> getTachesEnRetardListe() {
        if (taches == null) return List.of();
        return taches.stream().filter(OnboardingTacheModel::isEnRetard).toList();
    }

    public List<OnboardingTacheModel> getTachesBloqueesListe() {
        if (taches == null) return List.of();
        return taches.stream().filter(OnboardingTacheModel::isBloquee).toList();
    }

    public String getStatutLibelle() {
        if (statut == null) return "—";
        return switch (statut) {
            case "A_PREPARER" -> "À préparer";
            case "PREPARATION" -> "En préparation";
            case "PRET" -> "Prêt";
            case "EN_COURS" -> "En cours";
            case "BLOQUE" -> "Bloqué";
            case "A_VERIFIER" -> "À vérifier";
            case "CLOTURE" -> "Clôturé";
            case "ANNULE" -> "Annulé";
            default -> statut;
        };
    }

    public String getStatutBadge() {
        if (statut == null) return "badge";
        return switch (statut) {
            case "CLOTURE" -> "badge-ok";
            case "BLOQUE" -> "badge-bad";
            case "A_VERIFIER", "EN_COURS", "PRET" -> "badge-warn";
            default -> "badge-info";
        };
    }

    public String getDateDebutPrevueFr() { return Formats.date(dateDebutPrevue); }
    public String getDateDebutReelleFr() { return Formats.date(dateDebutReelle); }
    public String getDateFinPrevueFr() { return Formats.date(dateFinPrevue); }
    public String getDateClotureFr() { return Formats.dateHeure(dateCloture); }
}
