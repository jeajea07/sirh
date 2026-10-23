package com.gestionrh.sirh.model;

import jakarta.persistence.*;

@Entity
@Table(name = "employe")
public class EmployeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employe")
    private Integer id;

    @Column(name = "matricule", nullable = false)
    private String matricule;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", nullable = false)
    private String email;

    @ManyToOne
    @JoinColumn(name = "id_departement", nullable = false)
    private DepartementModel departement;

    @ManyToOne
    @JoinColumn(name = "id_poste")
    private PosteModel poste;

    @Column(name = "actif", nullable = false)
    private boolean actif = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getMatricule() { return matricule; }
    public void setMatricule(String matricule) { this.matricule = matricule; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public DepartementModel getDepartement() { return departement; }
    public void setDepartement(DepartementModel departement) { this.departement = departement; }

    public PosteModel getPoste() { return poste; }
    public void setPoste(PosteModel poste) { this.poste = poste; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getNomComplet() { return prenom + " " + nom; }
}
