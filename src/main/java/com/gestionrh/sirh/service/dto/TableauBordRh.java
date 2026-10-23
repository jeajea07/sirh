package com.gestionrh.sirh.service.dto;

import java.util.List;

public class TableauBordRh {

    private long contratsEnPreparation;
    private long contratsAValider;
    private long contratsASigner;
    private long contratsEcheance30j;
    private long arrivees30j;
    private long onboardingsEnCours;
    private long onboardingsACloturer;
    private long tachesEnRetard;
    private long formalitesEnAttente;
    private long documentsARevoir;
    private List<ContratEcheance> prochainesEcheances;
    private List<OnboardingResume> onboardingsRecents;
    private List<TacheVue> tachesEnRetardListe;

    public long getContratsEnPreparation() { return contratsEnPreparation; }
    public void setContratsEnPreparation(long contratsEnPreparation) { this.contratsEnPreparation = contratsEnPreparation; }
    public long getContratsAValider() { return contratsAValider; }
    public void setContratsAValider(long contratsAValider) { this.contratsAValider = contratsAValider; }
    public long getContratsASigner() { return contratsASigner; }
    public void setContratsASigner(long contratsASigner) { this.contratsASigner = contratsASigner; }
    public long getContratsEcheance30j() { return contratsEcheance30j; }
    public void setContratsEcheance30j(long contratsEcheance30j) { this.contratsEcheance30j = contratsEcheance30j; }
    public long getArrivees30j() { return arrivees30j; }
    public void setArrivees30j(long arrivees30j) { this.arrivees30j = arrivees30j; }
    public long getOnboardingsEnCours() { return onboardingsEnCours; }
    public void setOnboardingsEnCours(long onboardingsEnCours) { this.onboardingsEnCours = onboardingsEnCours; }
    public long getOnboardingsACloturer() { return onboardingsACloturer; }
    public void setOnboardingsACloturer(long onboardingsACloturer) { this.onboardingsACloturer = onboardingsACloturer; }
    public long getTachesEnRetard() { return tachesEnRetard; }
    public void setTachesEnRetard(long tachesEnRetard) { this.tachesEnRetard = tachesEnRetard; }
    public long getFormalitesEnAttente() { return formalitesEnAttente; }
    public void setFormalitesEnAttente(long formalitesEnAttente) { this.formalitesEnAttente = formalitesEnAttente; }
    public long getDocumentsARevoir() { return documentsARevoir; }
    public void setDocumentsARevoir(long documentsARevoir) { this.documentsARevoir = documentsARevoir; }
    public List<ContratEcheance> getProchainesEcheances() { return prochainesEcheances; }
    public void setProchainesEcheances(List<ContratEcheance> prochainesEcheances) { this.prochainesEcheances = prochainesEcheances; }
    public List<OnboardingResume> getOnboardingsRecents() { return onboardingsRecents; }
    public void setOnboardingsRecents(List<OnboardingResume> onboardingsRecents) { this.onboardingsRecents = onboardingsRecents; }
    public List<TacheVue> getTachesEnRetardListe() { return tachesEnRetardListe; }
    public void setTachesEnRetardListe(List<TacheVue> tachesEnRetardListe) { this.tachesEnRetardListe = tachesEnRetardListe; }
}
