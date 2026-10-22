package com.gestionrh.sirh.service;

/** Demande introuvable, appartenant à quelqu'un d'autre, ou dans un état qui interdit l'action. */
public class DemandeException extends RuntimeException {

    public DemandeException(String message) {
        super(message);
    }
}
