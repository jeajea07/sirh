package com.gestionrh.sirh.service.dto;

import java.util.List;

public class OnboardingDetail {

    private OnboardingVue onboarding;
    private List<TacheVue> taches;
    private int tachesTerminees;
    private int tachesEnRetard;
    private int tachesBloquees;
    private int tachesRestantes;
    private boolean peutCloturer;
    private List<String> raisonsNonCloture;
    private List<EvenementAuditVue> historique;

    public OnboardingVue getOnboarding() { return onboarding; }
    public void setOnboarding(OnboardingVue onboarding) { this.onboarding = onboarding; }
    public List<TacheVue> getTaches() { return taches; }
    public void setTaches(List<TacheVue> taches) { this.taches = taches; }
    public int getTachesTerminees() { return tachesTerminees; }
    public void setTachesTerminees(int tachesTerminees) { this.tachesTerminees = tachesTerminees; }
    public int getTachesEnRetard() { return tachesEnRetard; }
    public void setTachesEnRetard(int tachesEnRetard) { this.tachesEnRetard = tachesEnRetard; }
    public int getTachesBloquees() { return tachesBloquees; }
    public void setTachesBloquees(int tachesBloquees) { this.tachesBloquees = tachesBloquees; }
    public int getTachesRestantes() { return tachesRestantes; }
    public void setTachesRestantes(int tachesRestantes) { this.tachesRestantes = tachesRestantes; }
    public boolean isPeutCloturer() { return peutCloturer; }
    public void setPeutCloturer(boolean peutCloturer) { this.peutCloturer = peutCloturer; }
    public List<String> getRaisonsNonCloture() { return raisonsNonCloture; }
    public void setRaisonsNonCloture(List<String> raisonsNonCloture) { this.raisonsNonCloture = raisonsNonCloture; }
    public List<EvenementAuditVue> getHistorique() { return historique; }
    public void setHistorique(List<EvenementAuditVue> historique) { this.historique = historique; }
}
