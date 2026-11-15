package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gestionrh.sirh.model.AffectationModel;
import com.gestionrh.sirh.model.DepartementModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.PosteModel;
import com.gestionrh.sirh.repository.AffectationRepository;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.service.dto.AffectationForm;

class Bloc4AffectationServiceTest {

    private final AffectationRepository affectations = mock(AffectationRepository.class);
    private final AffectationService service = service();

    /** Régression : un changement d'affectation clôture l'affectation active, il ne doit pas être refusé. */
    @Test
    void unChangementDAffectationNExclutQueLAffectationActiveAnterieure() {
        AffectationModel active = new AffectationModel();
        active.setId(10);
        active.setStatut("ACTIVE");
        active.setDateDebut(LocalDate.of(2026, 1, 1));
        when(affectations.findFirstByIdEmployeAndStatutOrderByDateDebutDesc(1, "ACTIVE")).thenReturn(Optional.of(active));
        when(affectations.countChevauchements(eq(1), eq(10), any(), any())).thenReturn(0L);
        when(affectations.countChevauchements(eq(1), eq(null), any(), any())).thenReturn(1L);

        assertThat(service.verifier(formulaire(LocalDate.of(2026, 6, 1)))).isEmpty();
    }

    @Test
    void uneAffectationCommencantAvantLActiveResteRefusee() {
        AffectationModel active = new AffectationModel();
        active.setId(10);
        active.setStatut("ACTIVE");
        active.setDateDebut(LocalDate.of(2026, 6, 1));
        when(affectations.findFirstByIdEmployeAndStatutOrderByDateDebutDesc(1, "ACTIVE")).thenReturn(Optional.of(active));
        when(affectations.countChevauchements(eq(1), eq(null), any(), any())).thenReturn(1L);

        assertThat(service.verifier(formulaire(LocalDate.of(2026, 1, 1))))
                .contains("Une affectation active existe déjà sur cette période.");
    }

    private AffectationForm formulaire(LocalDate debut) {
        AffectationForm f = new AffectationForm();
        f.setIdEmploye(1);
        f.setIdPoste(2);
        f.setIdDepartement(3);
        f.setDateDebut(debut);
        return f;
    }

    private AffectationService service() {
        EmployeRepository employes = mock(EmployeRepository.class);
        PosteRepository postes = mock(PosteRepository.class);
        DepartementRepository departements = mock(DepartementRepository.class);
        when(employes.findById(1)).thenReturn(Optional.of(new EmployeModel()));
        when(postes.findById(2)).thenReturn(Optional.of(new PosteModel()));
        when(departements.findById(3)).thenReturn(Optional.of(new DepartementModel()));
        return new AffectationService(affectations, employes, postes, departements,
                mock(ContratRepository.class), mock(AuditService.class), mock(CarriereService.class));
    }
}
