package com.gestionrh.sirh.service.dto;

import java.util.ArrayList;
import java.util.List;

public class ModeleOnboardingForm {

    private Integer id;
    private String nom;
    private String description;
    private Integer idDepartement;
    private Integer idPoste;
    private List<ModeleTacheForm> taches = new ArrayList<>(List.of(new ModeleTacheForm()));

    public ModeleOnboardingForm() {
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getIdDepartement() { return idDepartement; }
    public void setIdDepartement(Integer idDepartement) { this.idDepartement = idDepartement; }
    public Integer getIdPoste() { return idPoste; }
    public void setIdPoste(Integer idPoste) { this.idPoste = idPoste; }
    public List<ModeleTacheForm> getTaches() { return taches; }
    public void setTaches(List<ModeleTacheForm> taches) {
        this.taches = taches == null ? new ArrayList<>(List.of(new ModeleTacheForm())) : taches;
    }
}
