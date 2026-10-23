// Bloc 3
package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PropositionForm {
    private String intitulePoste;
    private Integer idTypeContrat;
    private BigDecimal salaireMensuel;
    private LocalDate dateDebutPrevue;
    private Integer dureeMois;
    private LocalDate dateLimiteReponse;

    public String getIntitulePoste() { return intitulePoste; }
    public void setIntitulePoste(String v) { intitulePoste = v; }
    public Integer getIdTypeContrat() { return idTypeContrat; }
    public void setIdTypeContrat(Integer v) { idTypeContrat = v; }
    public BigDecimal getSalaireMensuel() { return salaireMensuel; }
    public void setSalaireMensuel(BigDecimal v) { salaireMensuel = v; }
    public LocalDate getDateDebutPrevue() { return dateDebutPrevue; }
    public void setDateDebutPrevue(LocalDate v) { dateDebutPrevue = v; }
    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer v) { dureeMois = v; }
    public LocalDate getDateLimiteReponse() { return dateLimiteReponse; }
    public void setDateLimiteReponse(LocalDate v) { dateLimiteReponse = v; }
}
