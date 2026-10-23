package com.gestionrh.sirh.service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AffectationForm {

    private Integer id;
    private Integer idEmploye;
    private Integer idPoste;
    private Integer idDepartement;
    private Integer idContrat;
    private Integer idResponsable;
    private String site;
    private String motif;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }
    public Integer getIdPoste() { return idPoste; }
    public void setIdPoste(Integer idPoste) { this.idPoste = idPoste; }
    public Integer getIdDepartement() { return idDepartement; }
    public void setIdDepartement(Integer idDepartement) { this.idDepartement = idDepartement; }
    public Integer getIdContrat() { return idContrat; }
    public void setIdContrat(Integer idContrat) { this.idContrat = idContrat; }
    public Integer getIdResponsable() { return idResponsable; }
    public void setIdResponsable(Integer idResponsable) { this.idResponsable = idResponsable; }
    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }
    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }
}
