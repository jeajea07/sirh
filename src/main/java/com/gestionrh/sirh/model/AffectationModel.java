package com.gestionrh.sirh.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gestionrh.sirh.util.Formats;

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
@Table(name = "affectation")
public class AffectationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_affectation")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poste", nullable = false)
    private PosteModel poste;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departement", nullable = false)
    private DepartementModel departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_contrat")
    private ContratModel contrat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_responsable")
    private EmployeModel responsable;

    @Column(name = "site")
    private String site;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "motif")
    private String motif;

    @Column(name = "statut", nullable = false)
    private String statut = "ACTIVE";

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id", nullable = false)
    private EmployeModel creePar;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public PosteModel getPoste() { return poste; }
    public void setPoste(PosteModel poste) { this.poste = poste; }

    public DepartementModel getDepartement() { return departement; }
    public void setDepartement(DepartementModel departement) { this.departement = departement; }

    public ContratModel getContrat() { return contrat; }
    public void setContrat(ContratModel contrat) { this.contrat = contrat; }

    public EmployeModel getResponsable() { return responsable; }
    public void setResponsable(EmployeModel responsable) { this.responsable = responsable; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate dateDebut) { this.dateDebut = dateDebut; }

    public LocalDate getDateFin() { return dateFin; }
    public void setDateFin(LocalDate dateFin) { this.dateFin = dateFin; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public EmployeModel getCreePar() { return creePar; }
    public void setCreePar(EmployeModel creePar) { this.creePar = creePar; }

    public boolean isActive() { return "ACTIVE".equals(statut); }

    public String getStatutLibelle() {
        return isActive() ? "Active" : "Terminée";
    }

    public String getStatutBadge() {
        return isActive() ? "badge-ok" : "badge";
    }

    public String getPeriodeLibelle() {
        if (dateDebut == null) return "—";
        if (dateFin == null) return "depuis le " + Formats.date(dateDebut);
        return Formats.date(dateDebut) + " → " + Formats.date(dateFin);
    }
}
