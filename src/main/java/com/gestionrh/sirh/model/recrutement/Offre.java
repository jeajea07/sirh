package com.gestionrh.sirh.recrutement.model;

import com.gestionrh.sirh.recrutement.enums.OffreStatut;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Offre d'emploi, creee depuis une demande acceptee (bloc 1). Les id_* externes (poste, departement...) restent des Integer : les entites correspondantes appartiennent a d'autres blocs. */
@Entity
@Table(name = "offre")
public class Offre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idOffre;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false)
    private Integer idDemandeBesoin;

    @Column(nullable = false)
    private Integer idPoste;

    @Column(nullable = false)
    private Integer idDepartement;

    @Column(nullable = false)
    private Integer idTypeContrat;

    private Integer dureeMois;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "text")
    private String description;

    @Column(columnDefinition = "text")
    private String missions;

    @PositiveOrZero
    @Column(precision = 14, scale = 2)
    private BigDecimal salairePropose;

    @Min(1)
    @Column(nullable = false)
    private Integer nombrePostes = 1;

    @Column(nullable = false)
    private Integer nbCandidatures = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OffreStatut statut = OffreStatut.BROUILLON;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    private Integer idValidateur;

    private LocalDateTime dateValidation;

    private LocalDateTime datePublication;

    private LocalDate dateLimite;

    @OneToMany(mappedBy = "offre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CritereOffre> criteres = new ArrayList<>();

    public Integer getIdOffre() { return idOffre; }
    public void setIdOffre(Integer idOffre) { this.idOffre = idOffre; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public Integer getIdDemandeBesoin() { return idDemandeBesoin; }
    public void setIdDemandeBesoin(Integer idDemandeBesoin) { this.idDemandeBesoin = idDemandeBesoin; }

    public Integer getIdPoste() { return idPoste; }
    public void setIdPoste(Integer idPoste) { this.idPoste = idPoste; }

    public Integer getIdDepartement() { return idDepartement; }
    public void setIdDepartement(Integer idDepartement) { this.idDepartement = idDepartement; }

    public Integer getIdTypeContrat() { return idTypeContrat; }
    public void setIdTypeContrat(Integer idTypeContrat) { this.idTypeContrat = idTypeContrat; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMissions() { return missions; }
    public void setMissions(String missions) { this.missions = missions; }

    public BigDecimal getSalairePropose() { return salairePropose; }
    public void setSalairePropose(BigDecimal salairePropose) { this.salairePropose = salairePropose; }

    public Integer getNombrePostes() { return nombrePostes; }
    public void setNombrePostes(Integer nombrePostes) { this.nombrePostes = nombrePostes; }

    public Integer getNbCandidatures() { return nbCandidatures; }
    public void setNbCandidatures(Integer nbCandidatures) { this.nbCandidatures = nbCandidatures; }

    public OffreStatut getStatut() { return statut; }
    public void setStatut(OffreStatut statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Integer getIdValidateur() { return idValidateur; }
    public void setIdValidateur(Integer idValidateur) { this.idValidateur = idValidateur; }

    public LocalDateTime getDateValidation() { return dateValidation; }
    public void setDateValidation(LocalDateTime dateValidation) { this.dateValidation = dateValidation; }

    public LocalDateTime getDatePublication() { return datePublication; }
    public void setDatePublication(LocalDateTime datePublication) { this.datePublication = datePublication; }

    public LocalDate getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDate dateLimite) { this.dateLimite = dateLimite; }

    public List<CritereOffre> getCriteres() { return criteres; }
    public void setCriteres(List<CritereOffre> criteres) { this.criteres = criteres; }

    public void addCritere(CritereOffre c) { criteres.add(c); c.setOffre(this); }
    public void removeCritere(CritereOffre c) { criteres.remove(c); c.setOffre(null); }
}
