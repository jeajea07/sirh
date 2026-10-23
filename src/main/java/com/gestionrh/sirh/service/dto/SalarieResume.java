package com.gestionrh.sirh.service.dto;

import java.time.LocalDate;

import com.gestionrh.sirh.util.Formats;

public class SalarieResume {

    private final Integer id;
    private final String matricule;
    private final String nom;
    private final String prenom;
    private final String email;
    private final String telephone;
    private final String poste;
    private final String departement;
    private final LocalDate dateEmbauche;
    private final boolean actif;
    private final String initiales;

    public SalarieResume(Integer id, String matricule, String nom, String prenom, String email,
                         String telephone, String poste, String departement, LocalDate dateEmbauche,
                         boolean actif, String initiales) {
        this.id = id;
        this.matricule = matricule;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.poste = poste;
        this.departement = departement;
        this.dateEmbauche = dateEmbauche;
        this.actif = actif;
        this.initiales = initiales;
    }

    public Integer getId() { return id; }
    public String getMatricule() { return matricule; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
    public String getNomComplet() { return prenom + " " + nom; }
    public String getEmail() { return email; }
    public String getTelephone() { return telephone; }
    public String getPoste() { return poste; }
    public String getDepartement() { return departement; }
    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public String getDateEmbaucheFr() { return Formats.date(dateEmbauche); }
    public boolean isActif() { return actif; }
    public String getStatutLibelle() { return actif ? "Actif" : "Sorti"; }
    public String getStatutBadge() { return actif ? "badge-ok" : "badge-bad"; }
    public String getInitiales() { return initiales; }
}
