package com.gestionrh.sirh.dto.recrutement;

import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

/** CV numérique public : DTO séparé des entités de persistance. */
public class CandidatureForm {

    @NotBlank @Size(max = 100) private String nom;
    @NotBlank @Size(max = 100) private String prenom;
    @NotBlank @Email @Size(max = 150) private String email;
    @Size(max = 30) private String telephone;
    @Size(max = 255) private String adresse;
    private NiveauEtude niveauEtude;
    @Min(0) private Integer anneesExperience;
    private String formations;
    private String experiences;
    private List<@Size(max = 100) String> competences = new ArrayList<>();
    @Size(max = 300) private String langues;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) private LocalDate disponibilite;
    private MultipartFile fichierCv;

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
    public NiveauEtude getNiveauEtude() { return niveauEtude; }
    public void setNiveauEtude(NiveauEtude niveauEtude) { this.niveauEtude = niveauEtude; }
    public Integer getAnneesExperience() { return anneesExperience; }
    public void setAnneesExperience(Integer anneesExperience) { this.anneesExperience = anneesExperience; }
    public String getFormations() { return formations; }
    public void setFormations(String formations) { this.formations = formations; }
    public String getExperiences() { return experiences; }
    public void setExperiences(String experiences) { this.experiences = experiences; }
    public List<String> getCompetences() { return competences; }
    public void setCompetences(List<String> competences) { this.competences = competences == null ? new ArrayList<>() : competences; }
    public String getLangues() { return langues; }
    public void setLangues(String langues) { this.langues = langues; }
    public LocalDate getDisponibilite() { return disponibilite; }
    public void setDisponibilite(LocalDate disponibilite) { this.disponibilite = disponibilite; }
    public MultipartFile getFichierCv() { return fichierCv; }
    public void setFichierCv(MultipartFile fichierCv) { this.fichierCv = fichierCv; }
}
