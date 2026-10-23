package com.gestionrh.sirh.model;

import java.time.LocalDateTime;

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
@Table(name = "modele_tache_onboarding")
public class ModeleTacheOnboardingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modele_tache_onboarding")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modele_onboarding", nullable = false)
    private ModeleOnboardingModel modele;

    @Column(name = "titre", nullable = false)
    private String titre;

    @Column(name = "description")
    private String description;

    @Column(name = "role_responsable", nullable = false)
    private String roleResponsable;

    @Column(name = "delai_indicatif_jours", nullable = false)
    private Integer delaiIndicatifJours = 0;

    @Column(name = "obligatoire", nullable = false)
    private boolean obligatoire = true;

    @Column(name = "bloquante", nullable = false)
    private boolean bloquante = false;

    @Column(name = "ordre", nullable = false)
    private Integer ordre = 0;

    @Column(name = "condition_activation", nullable = false)
    private String conditionActivation = "TOUJOURS";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depend_de_tache_id")
    private ModeleTacheOnboardingModel dependDeTache;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public ModeleOnboardingModel getModele() { return modele; }
    public void setModele(ModeleOnboardingModel modele) { this.modele = modele; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRoleResponsable() { return roleResponsable; }
    public void setRoleResponsable(String roleResponsable) { this.roleResponsable = roleResponsable; }

    public Integer getDelaiIndicatifJours() { return delaiIndicatifJours; }
    public void setDelaiIndicatifJours(Integer delaiIndicatifJours) { this.delaiIndicatifJours = delaiIndicatifJours; }

    public boolean isObligatoire() { return obligatoire; }
    public void setObligatoire(boolean obligatoire) { this.obligatoire = obligatoire; }

    public boolean isBloquante() { return bloquante; }
    public void setBloquante(boolean bloquante) { this.bloquante = bloquante; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public String getConditionActivation() { return conditionActivation; }
    public void setConditionActivation(String conditionActivation) { this.conditionActivation = conditionActivation; }

    public ModeleTacheOnboardingModel getDependDeTache() { return dependDeTache; }
    public void setDependDeTache(ModeleTacheOnboardingModel dependDeTache) { this.dependDeTache = dependDeTache; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

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
