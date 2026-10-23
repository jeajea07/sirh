package com.gestionrh.sirh.dto.recrutement;

import com.gestionrh.sirh.model.recrutement.Offre;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Donnees saisies pour une offre. On n'expose jamais l'entite directement au formulaire :
 * le statut, la reference et les dates de validation ne peuvent pas etre modifies par le navigateur.
 */
public class OffreForm {

    // Fixes a la creation (non modifiables ensuite)
    private Integer idDemandeBesoin;
    private Integer idPoste;
    private Integer idDepartement;
    private Integer idTypeContrat;

    @PositiveOrZero
    private Integer dureeMois;

    @NotBlank
    @Size(max = 200)
    private String titre;

    private String description;
    private String missions;

    @PositiveOrZero
    private BigDecimal salairePropose;

    @NotNull
    @Min(1)
    private Integer nombrePostes = 1;

    @Future
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateLimite;

    public static OffreForm depuis(Offre o) {
        OffreForm f = new OffreForm();
        f.idDemandeBesoin = o.getIdDemandeBesoin();
        f.idPoste = o.getIdPoste();
        f.idDepartement = o.getIdDepartement();
        f.idTypeContrat = o.getIdTypeContrat();
        f.dureeMois = o.getDureeMois();
        f.titre = o.getTitre();
        f.description = o.getDescription();
        f.missions = o.getMissions();
        f.salairePropose = o.getSalairePropose();
        f.nombrePostes = o.getNombrePostes();
        f.dateLimite = o.getDateLimite();
        return f;
    }

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
    public LocalDate getDateLimite() { return dateLimite; }
    public void setDateLimite(LocalDate dateLimite) { this.dateLimite = dateLimite; }
}
