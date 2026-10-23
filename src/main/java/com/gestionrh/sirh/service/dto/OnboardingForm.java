package com.gestionrh.sirh.service.dto;

import java.time.LocalDate;

public class OnboardingForm {

    private Integer idEmploye;
    private Integer idContrat;
    private Integer idModeleOnboarding;
    private LocalDate dateDebutPrevue;
    private LocalDate dateFinPrevue;
    private Integer idResponsableRh;

    public Integer getIdEmploye() { return idEmploye; }
    public void setIdEmploye(Integer idEmploye) { this.idEmploye = idEmploye; }
    public Integer getIdContrat() { return idContrat; }
    public void setIdContrat(Integer idContrat) { this.idContrat = idContrat; }
    public Integer getIdModeleOnboarding() { return idModeleOnboarding; }
    public void setIdModeleOnboarding(Integer idModeleOnboarding) { this.idModeleOnboarding = idModeleOnboarding; }
    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public void setDateDebutPrevue(LocalDate dateDebutPrevue) { this.dateDebutPrevue = dateDebutPrevue; }
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate dateFinPrevue) { this.dateFinPrevue = dateFinPrevue; }
    public Integer getIdResponsableRh() { return idResponsableRh; }
    public void setIdResponsableRh(Integer idResponsableRh) { this.idResponsableRh = idResponsableRh; }
}
