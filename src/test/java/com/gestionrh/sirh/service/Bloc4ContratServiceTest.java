package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.TypeContratModel;
import com.gestionrh.sirh.repository.AffectationRepository;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.repository.SignatureContratRepository;
import com.gestionrh.sirh.repository.TypeContratRepository;
import com.gestionrh.sirh.repository.ValidationB4Repository;
import com.gestionrh.sirh.service.dto.ContratForm;

class Bloc4ContratServiceTest {

    @Test
    void unCdiIgnoreLaDateDeFinSaisie() {
        ContratService service = serviceAvec("CDI");
        ContratForm form = formulaire("CDI");
        form.setDateFin(LocalDate.now().plusMonths(6));

        assertThat(service.verifier(form, true)).isEmpty();
        assertThat(form.getDateFin()).isNull();
    }

    @Test
    void unCddExigeUneDateDeFinPosterieureAuDebut() {
        ContratService service = serviceAvec("CDD");
        ContratForm form = formulaire("CDD");
        form.setDateDebut(LocalDate.of(2026, 10, 23));
        form.setDateFin(null);

        assertThat(service.verifier(form, true))
                .contains("La date de fin est obligatoire pour ce type de contrat.");

        form.setDateFin(LocalDate.of(2026, 4, 23));
        assertThat(service.verifier(form, true))
                .contains("La date de fin doit être postérieure à la date de début.");
    }

    private ContratService serviceAvec(String code) {
        EmployeRepository employes = mock(EmployeRepository.class);
        TypeContratRepository types = mock(TypeContratRepository.class);
        ContratRepository contrats = mock(ContratRepository.class);
        EmployeModel employe = new EmployeModel();
        employe.setId(1);
        employe.setActif(true);
        TypeContratModel type = new TypeContratModel();
        type.setId(1);
        type.setCode(code);
        when(employes.findById(1)).thenReturn(Optional.of(employe));
        when(types.findById(1)).thenReturn(Optional.of(type));
        when(contrats.existsByIdEmployeAndStatutWorkflowIn(1,
                java.util.List.of("EN_VIGUEUR", "SIGNE"))).thenReturn(false);
        return new ContratService(contrats, employes, types, mock(AffectationRepository.class),
                mock(SignatureContratRepository.class), mock(ValidationB4Repository.class),
                mock(PosteRepository.class), mock(DepartementRepository.class),
                mock(DocumentService.class), mock(AuditService.class),
                mock(NotificationService.class), mock(OnboardingService.class));
    }

    private ContratForm formulaire(String code) {
        ContratForm form = new ContratForm();
        form.setIdEmploye(1);
        form.setIdTypeContrat(1);
        form.setFonction("Comptable");
        form.setSalaire(new BigDecimal("1200000"));
        form.setDateDebut(LocalDate.of(2026, 10, 23));
        form.setTypeOperation("INITIAL");
        return form;
    }
}
