package com.gestionrh.sirh.service.recrutement;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Résolution provisoire depuis app.recrutement.rh-employe-ids. */
@Component
public class DestinatairesRhConfigurationResolver implements DestinatairesRhResolver {

    private final String identifiants;

    public DestinatairesRhConfigurationResolver(
            @Value("${app.recrutement.rh-employe-ids:}") String identifiants) {
        this.identifiants = identifiants;
    }

    @Override
    public List<Integer> trouverDestinataires() {
        if (identifiants == null || identifiants.isBlank()) return List.of();
        try {
            return Arrays.stream(identifiants.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::valueOf).distinct().toList();
        } catch (NumberFormatException e) {
            throw new IllegalStateException("app.recrutement.rh-employe-ids doit contenir des identifiants entiers séparés par des virgules.", e);
        }
    }
}
