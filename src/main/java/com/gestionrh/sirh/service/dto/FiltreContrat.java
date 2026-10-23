package com.gestionrh.sirh.service.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class FiltreContrat {

    private String statut;
    private String typeOperation;
    private String recherche;
    private Integer page = 0;
    private Integer taille = 10;

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getTypeOperation() { return typeOperation; }
    public void setTypeOperation(String typeOperation) { this.typeOperation = typeOperation; }
    public String getRecherche() { return recherche; }
    public void setRecherche(String recherche) { this.recherche = recherche; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getTaille() { return taille; }
    public void setTaille(Integer taille) { this.taille = taille; }

    public Pageable toPageable() {
        int p = page == null || page < 0 ? 0 : page;
        int t = taille == null || taille < 1 ? 10 : Math.min(taille, 50);
        return PageRequest.of(p, t, Sort.by("dateDebut").descending());
    }

    public boolean isVide() {
        return vide(statut) && vide(typeOperation) && vide(recherche);
    }

    private static boolean vide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
