package com.gestionrh.sirh.exception.recrutement;

/** Violation d'une regle metier (affichee a l'utilisateur telle quelle). */
public class RegleMetierException extends RuntimeException {
    public RegleMetierException(String message) {
        super(message);
    }
}
