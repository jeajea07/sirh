package com.gestionrh.sirh.service;

import java.math.BigDecimal;

public class VerificationFinanceForm {

    private BigDecimal salairePropose;
    private Integer dureeMois;
    private String commentaire;

    public BigDecimal getSalairePropose() { return salairePropose; }
    public void setSalairePropose(BigDecimal salairePropose) { this.salairePropose = salairePropose; }

    public Integer getDureeMois() { return dureeMois; }
    public void setDureeMois(Integer dureeMois) { this.dureeMois = dureeMois; }

    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
}
