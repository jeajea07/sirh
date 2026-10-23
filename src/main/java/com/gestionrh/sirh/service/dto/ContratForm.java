package com.gestionrh.sirh.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ContratForm {

    private Integer id;
    private Integer idEmploye;
    private Integer idTypeContrat;
    private String typeOperation = "INITIAL";
    private LocalDate dateSignature;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String fonction;
    private String categorieProfessionnelle;
    private BigDecimal salaire;
    private String motifRecours;
    private String commentaire;
    private Integer version;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }
    public Integer getIdTypeContrat() { return idTypeContrat; }
    public void setIdTypeContrat(Integer idTypeContrat) { this.idTypeContrat = idTypeContrat; }
    public String getTypeOperation() { return typeOperation; }
    public void setTypeOperation(String typeOperation) { this.typeOperation = typeOperation; }
    public LocalDate getDateSignature() { return dateSignature; }
    public void setDateSignature(LocalDate dateSignature) { this.dateSignature = dateSignature; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
    public String getFonction() { return fonction; }
    public void setFonction(String fonction) { this.fonction = fonction; }
    public String getCategorieProfessionnelle() { return categorieProfessionnelle; }
    public void setCategorieProfessionnelle(String categorieProfessionnelle) { this.categorieProfessionnelle = categorieProfessionnelle; }
    public BigDecimal getSalaire() { return salaire; }
    public void setSalaire(BigDecimal salaire) { this.salaire = salaire; }
    public String getMotifRecours() { return motifRecours; }
    public void setMotifRecours(String motifRecours) { this.motifRecours = motifRecours; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
