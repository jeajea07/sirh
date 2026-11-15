package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Destinataires RH des notifications du recrutement.
 * Priorité à app.recrutement.rh-employe-ids ; à défaut, tous les comptes RH actifs de la base
 * (sinon aucune notification « nouvelle offre », « candidature reçue » ou rappel ne serait envoyée).
 */
@Component
public class DestinatairesRhConfigurationResolver implements DestinatairesRhResolver {

    private final String identifiants;
    private final EmployeRepository employes;

    public DestinatairesRhConfigurationResolver(
            @Value("${app.recrutement.rh-employe-ids:}") String identifiants, EmployeRepository employes) {
        this.identifiants = identifiants;
        this.employes = employes;
    }

    @Override
    public List<Integer> trouverDestinataires() {
        if (identifiants == null || identifiants.isBlank()) {
            return employes.validateursParRole("RH").stream().map(EmployeModel::getId).distinct().toList();
        }
        try {
            return Arrays.stream(identifiants.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).map(Integer::valueOf).distinct().toList();
        } catch (NumberFormatException e) {
            throw new IllegalStateException("app.recrutement.rh-employe-ids doit contenir des identifiants entiers séparés par des virgules.", e);
        }
    }
}
