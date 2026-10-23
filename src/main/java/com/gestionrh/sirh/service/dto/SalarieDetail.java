package com.gestionrh.sirh.service.dto;

import java.time.LocalDate;
import java.util.List;

import com.gestionrh.sirh.util.Formats;

public class SalarieDetail {

    private SalarieResume salarie;
    private String cin;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String adresse;
    private String situationFamiliale;
    private Integer nombreEnfants;
    private String numeroCnaps;
    private String numeroOstie;
    private String rib;
    private List<ContratResume> contrats;
    private List<AffectationResume> affectations;
    private List<OnboardingResume> onboardings;
    private List<DocumentResume> documents;
    private List<DeclarationResume> formalites;
    private List<EvenementAuditVue> historique;

    public SalarieResume getSalarie() { return salarie; }
    public void setSalarie(SalarieResume salarie) { this.salarie = salarie; }
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getDateNaissanceFr() { return Formats.date(dateNaissance); }
    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public String getSituationFamiliale() { return situationFamiliale; }
    public void setSituationFamiliale(String situationFamiliale) { this.situationFamiliale = situationFamiliale; }
    public Integer getNombreEnfants() { return nombreEnfants; }
    public void setNombreEnfants(Integer nombreEnfants) { this.nombreEnfants = nombreEnfants; }
    public String getNumeroCnaps() { return numeroCnaps; }
    public void setNumeroCnaps(String numeroCnaps) { this.numeroCnaps = numeroCnaps; }
    public String getNumeroOstie() { return numeroOstie; }
    public void setNumeroOstie(String numeroOstie) { this.numeroOstie = numeroOstie; }
    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }
    public List<ContratResume> getContrats() { return contrats; }
    public void setContrats(List<ContratResume> contrats) { this.contrats = contrats; }
    public List<AffectationResume> getAffectations() { return affectations; }
    public void setAffectations(List<AffectationResume> affectations) { this.affectations = affectations; }
    public List<OnboardingResume> getOnboardings() { return onboardings; }
    public void setOnboardings(List<OnboardingResume> onboardings) { this.onboardings = onboardings; }
    public List<DocumentResume> getDocuments() { return documents; }
    public void setDocuments(List<DocumentResume> documents) { this.documents = documents; }
    public List<DeclarationResume> getFormalites() { return formalites; }
    public void setFormalites(List<DeclarationResume> formalites) { this.formalites = formalites; }
    public List<EvenementAuditVue> getHistorique() { return historique; }
    public void setHistorique(List<EvenementAuditVue> historique) { this.historique = historique; }
}
