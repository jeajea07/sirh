package com.gestionrh.sirh.model;

import com.gestionrh.sirh.util.Formats;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Pointage brut, jamais modifié ni supprimé (RG-PT01). */
@Entity
@Table(name = "pointage")
public class PointageModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pointage")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_employe", nullable = false)
    private EmployeModel employe;

    @Column(name = "horodatage", nullable = false)
    private LocalDateTime horodatage;

    @Column(name = "sens", nullable = false)
    private String sens;

    @Column(name = "source", nullable = false)
    private String source;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "saisi_par_id")
    private EmployeModel saisiPar;

    @Column(name = "motif")
    private String motif;

    @Column(name = "statut", nullable = false)
    private String statut = "VALIDE";

    @Column(name = "id_pointage_corrige")
    private Integer idPointageCorrige;

    @Column(name = "date_enregistrement", nullable = false)
    private LocalDateTime dateEnregistrement = java.time.LocalDateTime.now();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public EmployeModel getEmploye() { return employe; }
    public void setEmploye(EmployeModel employe) { this.employe = employe; }

    public LocalDateTime getHorodatage() { return horodatage; }
    public void setHorodatage(LocalDateTime horodatage) { this.horodatage = horodatage; }

    public String getSens() { return sens; }
    public void setSens(String sens) { this.sens = sens; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public EmployeModel getSaisiPar() { return saisiPar; }
    public void setSaisiPar(EmployeModel saisiPar) { this.saisiPar = saisiPar; }

    public String getMotif() { return motif; }
    public void setMotif(String motif) { this.motif = motif; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public Integer getIdPointageCorrige() { return idPointageCorrige; }
    public void setIdPointageCorrige(Integer idPointageCorrige) { this.idPointageCorrige = idPointageCorrige; }

    public LocalDateTime getDateEnregistrement() { return dateEnregistrement; }
    public void setDateEnregistrement(LocalDateTime dateEnregistrement) { this.dateEnregistrement = dateEnregistrement; }

    public String getHeureFr() { return horodatage.toLocalTime().withNano(0).toString(); }
    public String getHorodatageFr() { return Formats.dateHeure(horodatage); }
    public boolean isManuel() { return "MANUEL".equals(source); }
}
