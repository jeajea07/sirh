package com.gestionrh.sirh.service.recrutement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gestionrh.sirh.dto.recrutement.DemandeOffreResume;
import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.repository.DemandeBesoinRepository;
import com.gestionrh.sirh.repository.recrutement.OffreRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class OffreDepuisDemandeServiceTest {

    /**
     * Régression : le circuit déclare une étape RESPONSABLE que le bloc 1 ne parcourt pas.
     * Une demande validée par la DG (statut VALIDEE) doit pouvoir devenir une offre.
     */
    @Test
    void uneDemandeValideeParLaDgEstProposeePourUneOffre() {
        DemandeBesoinRepository demandes = mock(DemandeBesoinRepository.class);
        OffreRepository offres = mock(OffreRepository.class);
        when(demandes.findAll()).thenReturn(List.of(demande(1, "VALIDEE"), demande(2, "EN_VERIFICATION"),
                demande(3, "REJETEE")));
        when(offres.existsByIdDemandeBesoinAndStatutNotIn(anyInt(), any())).thenReturn(false);

        OffreDepuisDemandeService service = new OffreDepuisDemandeService(demandes, offres,
                mock(OffreService.class), mock(ApplicationEventPublisher.class));

        assertThat(service.demandesAccepteesSansOffre()).extracting(DemandeOffreResume::id).containsExactly(1);
    }

    private DemandeBesoinModel demande(int id, String statut) {
        DemandeBesoinModel d = new DemandeBesoinModel();
        d.setId(id);
        d.setStatut(statut);
        return d;
    }
}
