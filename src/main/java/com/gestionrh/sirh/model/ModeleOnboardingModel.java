package com.gestionrh.sirh.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "modele_onboarding", uniqueConstraints =
        @UniqueConstraint(name = "uq_modele_nom_version", columnNames = {"nom", "version"}))
public class ModeleOnboardingModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modele_onboarding")
    private Integer id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_departement")
    private DepartementModel departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_poste")
    private PosteModel poste;

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_maj", nullable = false)
    private LocalDateTime dateMaj;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cree_par_id", nullable = false)
    private EmployeModel creePar;

    @OneToMany(mappedBy = "modele", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC, id ASC")
    private List<ModeleTacheOnboardingModel> taches = new ArrayList<>();

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DepartementModel getDepartement() { return departement; }
    public void setDepartement(DepartementModel departement) { this.departement = departement; }

    public PosteModel getPoste() { return poste; }
    public void setPoste(PosteModel poste) { this.poste = poste; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateMaj() { return dateMaj; }
    public void setDateMaj(LocalDateTime dateMaj) { this.dateMaj = dateMaj; }

    public EmployeModel getCreePar() { return creePar; }
    public void setCreePar(EmployeModel creePar) { this.creePar = creePar; }

    public List<ModeleTacheOnboardingModel> getTaches() { return taches; }
    public void setTaches(List<ModeleTacheOnboardingModel> taches) { this.taches = taches; }

    public String getVersionLibelle() { return "Version " + version; }

    public boolean isModifiable() { return true; }

    public int getNombreTaches() { return taches == null ? 0 : taches.size(); }

    public int getDureeIndicativeJours() {
        if (taches == null) return 0;
        return taches.stream()
                .map(ModeleTacheOnboardingModel::getDelaiIndicatifJours)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
    }
}
