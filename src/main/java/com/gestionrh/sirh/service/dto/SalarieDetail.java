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

    // --- Bloc 5 : dossier administratif ---
    private String sexe;
    private String nationalite;
    private String contactUrgenceNom;
    private String contactUrgenceTelephone;
    private LocalDate dateNotificationSortie;
    private LocalDate dateSortie;
    private String motifSortie;
    private ContratResume contratEnVigueur;
    private com.gestionrh.sirh.model.HistoriqueSalaireModel salaireEnVigueur;
    private List<com.gestionrh.sirh.model.HistoriqueSalaireModel> historiqueSalaires = List.of();
    private List<com.gestionrh.sirh.model.EvenementCarriereModel> carriere = List.of();
    private List<PieceDossierVue> pieces = List.of();
    private List<String> alertes = List.of();
    private String anciennete;
    /** RG-SA07 / RG-AC03 : salaire et RIB visibles uniquement par les profils habilités. */
    private boolean donneesSensiblesVisibles;

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }
    public String getSexeLibelle() { return "M".equals(sexe) ? "Masculin" : "F".equals(sexe) ? "Féminin" : "—"; }
    public String getNationalite() { return nationalite; }
    public void setNationalite(String nationalite) { this.nationalite = nationalite; }
    public String getContactUrgenceNom() { return contactUrgenceNom; }
    public void setContactUrgenceNom(String contactUrgenceNom) { this.contactUrgenceNom = contactUrgenceNom; }
    public String getContactUrgenceTelephone() { return contactUrgenceTelephone; }
    public void setContactUrgenceTelephone(String contactUrgenceTelephone) { this.contactUrgenceTelephone = contactUrgenceTelephone; }
    public LocalDate getDateNotificationSortie() { return dateNotificationSortie; }
    public void setDateNotificationSortie(LocalDate dateNotificationSortie) { this.dateNotificationSortie = dateNotificationSortie; }
    public String getDateNotificationSortieFr() { return Formats.date(dateNotificationSortie); }
    public LocalDate getDateSortie() { return dateSortie; }
    public void setDateSortie(LocalDate dateSortie) { this.dateSortie = dateSortie; }
    public String getDateSortieFr() { return Formats.date(dateSortie); }
    public String getMotifSortie() { return motifSortie; }
    public void setMotifSortie(String motifSortie) { this.motifSortie = motifSortie; }
    public ContratResume getContratEnVigueur() { return contratEnVigueur; }
    public void setContratEnVigueur(ContratResume contratEnVigueur) { this.contratEnVigueur = contratEnVigueur; }
    public com.gestionrh.sirh.model.HistoriqueSalaireModel getSalaireEnVigueur() { return salaireEnVigueur; }
    public void setSalaireEnVigueur(com.gestionrh.sirh.model.HistoriqueSalaireModel salaireEnVigueur) { this.salaireEnVigueur = salaireEnVigueur; }
    public List<com.gestionrh.sirh.model.HistoriqueSalaireModel> getHistoriqueSalaires() { return historiqueSalaires; }
    public void setHistoriqueSalaires(List<com.gestionrh.sirh.model.HistoriqueSalaireModel> historiqueSalaires) { this.historiqueSalaires = historiqueSalaires; }
    public List<com.gestionrh.sirh.model.EvenementCarriereModel> getCarriere() { return carriere; }
    public void setCarriere(List<com.gestionrh.sirh.model.EvenementCarriereModel> carriere) { this.carriere = carriere; }
    public List<PieceDossierVue> getPieces() { return pieces; }
    public void setPieces(List<PieceDossierVue> pieces) { this.pieces = pieces; }
    public List<String> getAlertes() { return alertes; }
    public void setAlertes(List<String> alertes) { this.alertes = alertes; }
    public String getAnciennete() { return anciennete; }
    public void setAnciennete(String anciennete) { this.anciennete = anciennete; }
    public boolean isDonneesSensiblesVisibles() { return donneesSensiblesVisibles; }
    public void setDonneesSensiblesVisibles(boolean donneesSensiblesVisibles) { this.donneesSensiblesVisibles = donneesSensiblesVisibles; }
    public boolean isDossierIncomplet() { return pieces.stream().anyMatch(PieceDossierVue::isBloquante); }
    public long getNombrePiecesBloquantes() { return pieces.stream().filter(PieceDossierVue::isBloquante).count(); }
    public String getStatutAdministratif() { return salarie == null ? null : salarie.getStatutAdministratif(); }
    public boolean isSorti() { return "SORTI".equals(getStatutAdministratif()); }

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
