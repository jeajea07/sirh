package com.gestionrh.sirh.service.dto;

import java.time.LocalDate;

import com.gestionrh.sirh.util.Formats;

public class AffectationResume {

    private final Integer id;
    private final Integer idEmploye;
    private final String employeNom;
    private final String poste;
    private final String departement;
    private final String responsableNom;
    private final String site;
    private final String motif;
    private final LocalDate dateDebut;
    private final LocalDate dateFin;
    private final String statut;
    private final String statutLibelle;
    private final String statutBadge;

    public AffectationResume(Integer id, Integer idEmploye, String employeNom, String poste,
                             String departement, String responsableNom, String site, String motif,
                             LocalDate dateDebut, LocalDate dateFin, String statut,
                             String statutLibelle, String statutBadge) {
        this.id = id;
        this.idEmploye = idEmploye;
        this.employeNom = employeNom;
        this.poste = poste;
        this.departement = departement;
        this.responsableNom = responsableNom;
        this.site = site;
        this.motif = motif;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
        this.statutLibelle = statutLibelle;
        this.statutBadge = statutBadge;
    }

    public Integer getId() { return id; }
    public Integer getIdEmploye() { return idEmploye; }
    public String getEmployeNom() { return employeNom; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public String getResponsableNom() { return responsableNom; }
    public String getSite() { return site; }
    public String getMotif() { return motif; }
    public LocalDate getDateDebut() { return dateDebut; }
    public LocalDate getDateFin() { return dateFin; }
    public String getStatut() { return statut; }
    public String getStatutLibelle() { return statutLibelle; }
    public String getStatutBadge() { return statutBadge; }
    public boolean isActive() { return "ACTIVE".equals(statut); }
    public String getPeriodeLibelle() {
        if (dateDebut == null) return "—";
        return dateFin == null ? "depuis le " + Formats.date(dateDebut)
                : Formats.date(dateDebut) + " → " + Formats.date(dateFin);
    }
}
