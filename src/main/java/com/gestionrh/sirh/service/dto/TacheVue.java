package com.gestionrh.sirh.service.dto;

import java.time.LocalDate;

import com.gestionrh.sirh.util.Formats;

public class TacheVue {

    private final Integer id;
    private final Integer idOnboarding;
    private final String onboardingReference;
    private final String employeNom;
    private final String titre;
    private final String description;
    private final String roleLibelle;
    private final String responsableNom;
    private final LocalDate dateEcheance;
    private final String statut;
    private final String statutLibelle;
    private final String statutBadge;
    private final boolean obligatoire;
    private final boolean bloquante;
    private final boolean enRetard;
    private final boolean dependancePrete;
    private final String dependanceTitre;
    private final String commentaire;
    private final String motifBlocage;
    private final Integer ordre;

    public TacheVue(Integer id, Integer idOnboarding, String onboardingReference, String employeNom,
                    String titre, String description, String roleLibelle, String responsableNom,
                    LocalDate dateEcheance, String statut, String statutLibelle, String statutBadge,
                    boolean obligatoire, boolean bloquante, boolean enRetard,
                    boolean dependancePrete, String dependanceTitre, String commentaire,
                    String motifBlocage, Integer ordre) {
        this.id = id;
        this.idOnboarding = idOnboarding;
        this.onboardingReference = onboardingReference;
        this.employeNom = employeNom;
        this.titre = titre;
        this.description = description;
        this.roleLibelle = roleLibelle;
        this.responsableNom = responsableNom;
        this.dateEcheance = dateEcheance;
        this.statut = statut;
        this.statutLibelle = statutLibelle;
        this.statutBadge = statutBadge;
        this.obligatoire = obligatoire;
        this.bloquante = bloquante;
        this.enRetard = enRetard;
        this.dependancePrete = dependancePrete;
        this.dependanceTitre = dependanceTitre;
        this.commentaire = commentaire;
        this.motifBlocage = motifBlocage;
        this.ordre = ordre;
    }

    public Integer getId() { return id; }
    public Integer getIdOnboarding() { return idOnboarding; }
    public String getOnboardingReference() { return onboardingReference; }
    public String getEmployeNom() { return employeNom; }
    public String getTitre() { return titre; }
    public String getDescription() { return description; }
    public String getRoleLibelle() { return roleLibelle; }
    public String getResponsableNom() { return responsableNom; }
    public LocalDate getDateEcheance() { return dateEcheance; }
    public String getDateEcheanceFr() { return Formats.date(dateEcheance); }
    public String getStatut() { return statut; }
    public String getStatutLibelle() { return statutLibelle; }
    public String getStatutBadge() { return statutBadge; }
    public boolean isObligatoire() { return obligatoire; }
    public boolean isBloquante() { return bloquante; }
    public boolean isEnRetard() { return enRetard; }
    public boolean isDependancePrete() { return dependancePrete; }
    public String getDependanceTitre() { return dependanceTitre; }
    public String getCommentaire() { return commentaire; }
    public String getMotifBlocage() { return motifBlocage; }
    public Integer getOrdre() { return ordre; }
    public boolean isTerminee() { return "TERMINEE".equals(statut); }
    public boolean isAnnulee() { return "ANNULEE".equals(statut); }
    public boolean isBloquee() { return "BLOQUEE".equals(statut); }
}
