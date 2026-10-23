package com.gestionrh.sirh.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Données minimales transmises par le Bloc 3 au Bloc 4 pour créer une embauche. */
public class EmbaucheDTO {

    private Integer idRecrutement;
    private Integer idSelection;
    private Integer idCandidatureB2;
    private Integer idOffreB2;
    private Integer idCandidatB2;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String codeTypeContrat;
    private BigDecimal salaire;
    private LocalDate dateDebut;
    private Integer dureeMois;
    private String fonction;
    private Integer nombrePostes;
    private LocalDate dateConfirmation;
    private Integer idPoste;
    private Integer idDepartement;
    private String cin;
    private String adresse;
    private LocalDate dateNaissance;
    private String categorieProfessionnelle;
    private String motifRecours;
    private Integer idDocumentCv;

    public Integer getIdRecrutement() { return idRecrutement; }
    public void setIdRecrutement(Integer idRecrutement) { this.idRecrutement = idRecrutement; }
    public Integer getIdSelection() { return idSelection; }
    public void setIdSelection(Integer idSelection) { this.idSelection = idSelection; }
    public Integer getIdCandidatureB2() { return idCandidatureB2; }
    public void setIdCandidatureB2(Integer idCandidatureB2) { this.idCandidatureB2 = idCandidatureB2; }
    public Integer getIdOffreB2() { return idOffreB2; }
    public void setIdOffreB2(Integer idOffreB2) { this.idOffreB2 = idOffreB2; }
    public Integer getIdCandidatB2() { return idCandidatB2; }
    public void setIdCandidatB2(Integer idCandidatB2) { this.idCandidatB2 = idCandidatB2; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getCodeTypeContrat() { return codeTypeContrat; }
    public void setCodeTypeContrat(String codeTypeContrat) { this.codeTypeContrat = codeTypeContrat; }
    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }
    public String getFonction() { return fonction; }
    public void setFonction(String fonction) { this.fonction = fonction; }
    public Integer getNombrePostes() { return nombrePostes; }
    public void setNombrePostes(Integer nombrePostes) { this.nombrePostes = nombrePostes; }
    public LocalDate getDateConfirmation() { return dateConfirmation; }
    public void setDateConfirmation(LocalDate dateConfirmation) { this.dateConfirmation = dateConfirmation; }
    public Integer getIdPoste() { return idPoste; }
    public void setIdPoste(Integer idPoste) { this.idPoste = idPoste; }
    public Integer getIdDepartement() { return idDepartement; }
    public void setIdDepartement(Integer idDepartement) { this.idDepartement = idDepartement; }
    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getCategorieProfessionnelle() { return categorieProfessionnelle; }
    public void setCategorieProfessionnelle(String categorieProfessionnelle) { this.categorieProfessionnelle = categorieProfessionnelle; }
    public String getMotifRecours() { return motifRecours; }
    public void setMotifRecours(String motifRecours) { this.motifRecours = motifRecours; }
    public Integer getIdDocumentCv() { return idDocumentCv; }
    public void setIdDocumentCv(Integer idDocumentCv) { this.idDocumentCv = idDocumentCv; }
}
