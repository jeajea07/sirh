package com.gestionrh.sirh.model;

import java.time.LocalDate;
import java.time.Period;

import com.gestionrh.sirh.util.Formats;

import jakarta.persistence.*;

@Entity
@Table(name = "employe")
public class EmployeModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employe")
    private Integer id;

    @Column(name = "matricule", nullable = false, unique = true)
    private String matricule;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "prenom", nullable = false)
    private String prenom;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "cin", unique = true)
    private String cin;

    @Column(name = "date_naissance")
    private LocalDate dateNaissance;

    @Column(name = "lieu_naissance")
    private String lieuNaissance;

    @Column(name = "telephone")
    private String telephone;

    @Column(name = "adresse")
    private String adresse;

    @Column(name = "situation_familiale")
    private String situationFamiliale;

    @Column(name = "nombre_enfants")
    private Integer nombreEnfants = 0;

    @Column(name = "numero_cnaps")
    private String numeroCnaps;

    @Column(name = "numero_ostie")
    private String numeroOstie;

    @Column(name = "rib")
    private String rib;

    @Column(name = "date_embauche")
    private LocalDate dateEmbauche;

    @Column(name = "date_sortie")
    private LocalDate dateSortie;

    @Column(name = "motif_sortie")
    private String motifSortie;

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

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getLieuNaissance() { return lieuNaissance; }
    public void setLieuNaissance(String lieuNaissance) { this.lieuNaissance = lieuNaissance; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getSituationFamiliale() { return situationFamiliale; }
    public void setSituationFamiliale(String situationFamiliale) { this.situationFamiliale = situationFamiliale; }

    public Integer getNombreEnfants() { return nombreEnfants; }
    public void setNombreEnfants(Integer nombreEnfants) { this.nombreEnfants = nombreEnfants; }

    public String getNumeroCnaps() { return numeroCnaps; }
    public void setNumeroCnaps(String numeroCnaps) { this.numeroCnaps = numeroCnaps; }

    public String getNumeroOstie() { return numeroOstie; }
    public void setNumeroOstie(String numeroOstie) { this.numeroOstie = numeroOstie; }

    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }

    public LocalDate getDateEmbauche() { return dateEmbauche; }
    public void setDateEmbauche(LocalDate dateEmbauche) { this.dateEmbauche = dateEmbauche; }

    public LocalDate getDateSortie() { return dateSortie; }
    public void setDateSortie(LocalDate dateSortie) { this.dateSortie = dateSortie; }

    public String getMotifSortie() { return motifSortie; }
    public void setMotifSortie(String motifSortie) { this.motifSortie = motifSortie; }

    public DepartementModel getDepartement() { return departement; }
    public void setDepartement(DepartementModel departement) { this.departement = departement; }

    public PosteModel getPoste() { return poste; }
    public void setPoste(PosteModel poste) { this.poste = poste; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    public String getNomComplet() {
        return (prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom);
    }

    public String getInitiales() {
        String p = prenom == null || prenom.isBlank() ? "?" : prenom.substring(0, 1);
        String n = nom == null || nom.isBlank() ? "" : nom.substring(0, 1);
        return (p + n).toUpperCase();
    }

    public Integer getAge() {
        return dateNaissance == null ? null : Period.between(dateNaissance, LocalDate.now()).getYears();
    }

    public String getDateNaissanceFr() { return Formats.date(dateNaissance); }
    public String getDateEmbaucheFr() { return Formats.date(dateEmbauche); }

    public boolean isSorti() { return dateSortie != null || !actif; }
}
