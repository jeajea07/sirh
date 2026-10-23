package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.ModeleOnboardingModel;
import com.gestionrh.sirh.model.ModeleTacheOnboardingModel;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.ModeleOnboardingRepository;
import com.gestionrh.sirh.repository.ModeleTacheOnboardingRepository;
import com.gestionrh.sirh.repository.OnboardingRepository;
import com.gestionrh.sirh.repository.PosteRepository;

class Bloc4ModeleOnboardingServiceTest {

    @Test
    void dupliquerConserveLesDependancesEntreTaches() {
        ModeleTacheOnboardingModel premiere = new ModeleTacheOnboardingModel();
        premiere.setId(10);
        premiere.setTitre("Préparer le dossier");
        ModeleTacheOnboardingModel seconde = new ModeleTacheOnboardingModel();
        seconde.setId(11);
        seconde.setTitre("Valider le dossier");
        seconde.setDependDeTache(premiere);

        ModeleOnboardingModel source = new ModeleOnboardingModel();
        source.setId(1);
        source.setNom("Standard");
        source.setVersion(1);
        source.setTaches(new ArrayList<>(List.of(premiere, seconde)));

        ModeleOnboardingRepository modeles = mock(ModeleOnboardingRepository.class);
        when(modeles.findAvecTachesById(1)).thenReturn(Optional.of(source));
        when(modeles.findByNomOrderByVersionDesc("Standard")).thenReturn(List.of(source));
        when(modeles.save(any(ModeleOnboardingModel.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ModeleOnboardingService service = new ModeleOnboardingService(modeles,
                mock(ModeleTacheOnboardingRepository.class), mock(OnboardingRepository.class),
                mock(DepartementRepository.class), mock(PosteRepository.class), mock(AuditService.class));

        ModeleOnboardingModel copie = service.dupliquer(1, new EmployeModel());

        assertThat(copie.getVersion()).isEqualTo(2);
        assertThat(copie.getTaches()).hasSize(2);
        assertThat(copie.getTaches().get(1).getDependDeTache())
                .isSameAs(copie.getTaches().get(0));
    }
}
