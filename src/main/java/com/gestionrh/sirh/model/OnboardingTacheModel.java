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
@Table(name = "onboarding_tache")
public class OnboardingTacheModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_onboarding_tache")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_onboarding", nullable = false)
    private OnboardingModel onboarding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modele_tache_onboarding")
    private ModeleTacheOnboardingModel modeleTache;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "description")
    private String description;

    @Column(name = "role_responsable", nullable = false)
    private String roleResponsable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private EmployeModel responsable;

    @Column(name = "date_echeance")
    private LocalDate dateEcheance;

    @Column(name = "statut", nullable = false)
    private String statut = "A_FAIRE";

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_realisation")
    private LocalDateTime dateRealisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "realise_par_id")
    private EmployeModel realisePar;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "motif_blocage")
    private String motifBlocage;

    @Column(name = "obligatoire", nullable = false)
    private boolean obligatoire = true;

    @Column(name = "bloquante", nullable = false)
    private boolean bloquante = false;

    @Column(name = "ordre", nullable = false)
    private Integer ordre = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depend_de_tache_id")
    private OnboardingTacheModel dependDeTache;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public OnboardingModel getOnboarding() { return onboarding; }
    public void setOnboarding(OnboardingModel onboarding) { this.onboarding = onboarding; }

    public ModeleTacheOnboardingModel getModeleTache() { return modeleTache; }
    public void setModeleTache(ModeleTacheOnboardingModel modeleTache) { this.modeleTache = modeleTache; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoleResponsable() { return roleResponsable; }
    public void setRoleResponsable(String roleResponsable) { this.roleResponsable = roleResponsable; }

    public EmployeModel getResponsable() { return responsable; }
    public void setResponsable(EmployeModel responsable) { this.responsable = responsable; }

    public LocalDate getDateEcheance() { return dateEcheance; }
    public void setDateEcheance(LocalDate dateEcheance) { this.dateEcheance = dateEcheance; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDateTime dateDebut) { this.dateDebut = dateDebut; }

    public LocalDateTime getDateRealisation() { return dateRealisation; }
    public void setDateRealisation(LocalDateTime dateRealisation) { this.dateRealisation = dateRealisation; }

    public EmployeModel getRealisePar() { return realisePar; }
    public void setRealisePar(EmployeModel realisePar) { this.realisePar = realisePar; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }

    public String getMotifBlocage() { return motifBlocage; }
    public void setMotifBlocage(String motifBlocage) { this.motifBlocage = motifBlocage; }

    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }

    public boolean isBloquante() { return bloquante; }
    public void setBloquante(boolean bloquante) { this.bloquante = bloquante; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public OnboardingTacheModel getDependDeTache() { return dependDeTache; }
    public void setDependDeTache(OnboardingTacheModel dependDeTache) { this.dependDeTache = dependDeTache; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public boolean isTerminee() { return "TERMINEE".equals(statut); }
    public boolean isBloquee() { return "BLOQUEE".equals(statut); }

    public boolean isEnRetard() {
        return dateEcheance != null && dateEcheance.isBefore(LocalDate.now())
                && !isTerminee() && !"ANNULEE".equals(statut);
    }

    public boolean isDependancePrete() {
        return dependDeTache == null || dependDeTache.isTerminee();
    }

    public String getStatutLibelle() {
        if (statut == null) return "—";
        return switch (statut) {
            case "A_FAIRE" -> "À faire";
            case "EN_COURS" -> "En cours";
            case "TERMINEE" -> "Terminée";
            case "BLOQUEE" -> "Bloquée";
            case "ANNULEE" -> "Annulée";
            default -> statut;
        };
    }

    public String getStatutBadge() {
        if (statut == null) return "badge";
        return switch (statut) {
            case "TERMINEE" -> "badge-ok";
            case "BLOQUEE" -> "badge-bad";
            case "EN_COURS" -> "badge-warn";
            default -> "badge-info";
        };
    }

    public String getDateEcheanceFr() { return Formats.date(dateEcheance); }

    public String getRoleLibelle() {
        if (roleResponsable == null) return "—";
        return switch (roleResponsable) {
            case "EMPLOYE" -> "Salarié";
            case "RESPONSABLE" -> "Responsable hiérarchique";
            case "RH" -> "Ressources humaines";
            case "FINANCE" -> "Finance";
            case "DG" -> "Direction générale";
            case "INFORMATIQUE" -> "Informatique";
            case "SERVICES_GENERAUX" -> "Services généraux";
            case "PAIE" -> "Paie";
            case "JURIDIQUE" -> "Juridique";
            case "SANTE" -> "Santé au travail";
            default -> roleResponsable;
        };
    }
}
