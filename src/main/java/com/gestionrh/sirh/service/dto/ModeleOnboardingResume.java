package com.gestionrh.sirh.service.dto;

import java.time.LocalDateTime;

import com.gestionrh.sirh.util.Formats;

public class ModeleOnboardingResume {

    private final Integer id;
    private final String nom;
    private final String description;
    private final Integer version;
    private final String poste;
    private final String departement;
    private final boolean actif;
    private final int nombreTaches;
    private final int dureeIndicativeJours;
    private final LocalDateTime dateMaj;

    public ModeleOnboardingResume(Integer id, String nom, String description, Integer version,
                                  String poste, String departement, boolean actif,
                                  int nombreTaches, int dureeIndicativeJours, LocalDateTime dateMaj) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.version = version;
        this.poste = poste;
        this.departement = departement;
        this.actif = actif;
        this.nombreTaches = nombreTaches;
        this.dureeIndicativeJours = dureeIndicativeJours;
        this.dateMaj = dateMaj;
    }

    public Integer getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
    public Integer getVersion() { return version; }
    public String getVersionLibelle() { return "Version " + version; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public boolean isActif() { return actif; }
    public String getActifLibelle() { return actif ? "Actif" : "Inactif"; }
    public String getActifBadge() { return actif ? "badge-ok" : "badge"; }
    public int getNombreTaches() { return nombreTaches; }
    public int getDureeIndicativeJours() { return dureeIndicativeJours; }
    public String getDateMajFr() { return Formats.dateHeure(dateMaj); }
}
