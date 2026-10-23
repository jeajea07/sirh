package com.gestionrh.sirh.service;

import java.util.List;
import java.util.Locale;

public record Bloc2BQuestion(String reference, String label, String type, String expectedAnswer) {

    public static final List<Bloc2BQuestion> QUESTIONS = List.of(
            new Bloc2BQuestion("Q1", "Quel est le mot-clé Java utilisé pour gérer des nombres décimaux précis ?", "CHOIX", "Java"),
            new Bloc2BQuestion("Q2", "Quelle classe Java est utilisée pour manipuler les montants financiers sans erreur d’arrondi ?", "CHOIX", "BigDecimal"),
            new Bloc2BQuestion("Q3", "En une phrase, expliquez pourquoi il faut valider une réponse avant de la finaliser.", "LIBRE", "validation")
    );

    public static Bloc2BQuestion findByReference(String questionRef) {
        return QUESTIONS.stream()
                .filter(question -> question.reference().equals(questionRef))
                .findFirst()
                .orElse(null);
    }

    public boolean matches(String value) {
        if (value == null) {
            return false;
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            return false;
        }

        String lower = normalized.toLowerCase(Locale.ROOT);
        String expected = expectedAnswer.toLowerCase(Locale.ROOT);

        if ("Q3".equals(reference)) {
            return lower.contains("java") && lower.contains(expected);
        }

        return lower.contains(expected) || normalized.equalsIgnoreCase(expectedAnswer);
    }
}
