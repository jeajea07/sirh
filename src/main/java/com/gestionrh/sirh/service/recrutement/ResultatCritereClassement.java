package com.gestionrh.sirh.service.recrutement;

public record ResultatCritereClassement(String nature, String libelle, EtatCritereClassement etat,
        String valeurCandidat, String detail) { }
