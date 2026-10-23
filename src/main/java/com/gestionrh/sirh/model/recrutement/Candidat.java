package com.gestionrh.sirh.model.recrutement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/** Identite du candidat. Le compte de connexion au test est gere par le bloc 2B. */
@Entity
@Table(name = "candidat")
public class Candidat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCandidat;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nom;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String prenom;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String telephone;

    @Column(length = 255)
    private String adresse;

    /** Identifiant de connexion au test technique, cree par le module test (ex. CAND12). */
    @Column(length = 100, unique = true)
    private String identifiant;

    /** Mot de passe BCrypt du compte candidat. */
    @Column(length = 255)
    private String motDePasse;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(name = "acces_cree", nullable = false)
    private boolean accesCree = false;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    public Integer getIdCandidat() { return idCandidat; }
    public void setIdCandidat(Integer idCandidat) { this.idCandidat = idCandidat; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getIdentifiant() { return identifiant; }
    public void setIdentifiant(String identifiant) { this.identifiant = identifiant; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public boolean isAccesCree() { return accesCree; }
    public void setAccesCree(boolean accesCree) { this.accesCree = accesCree; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }
}
