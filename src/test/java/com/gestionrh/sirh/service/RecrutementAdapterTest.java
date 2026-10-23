package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import com.gestionrh.sirh.model.recrutement.Candidat;
import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.EtatPropositionModel;
import com.gestionrh.sirh.model.EtatRecrutementModel;
import com.gestionrh.sirh.model.EtatTransfertModel;
import com.gestionrh.sirh.model.PropositionModel;
import com.gestionrh.sirh.model.RecrutementModel;
import com.gestionrh.sirh.model.ReponseCandidatModel;
import com.gestionrh.sirh.model.SelectionModel;
import com.gestionrh.sirh.model.TypeContratEmbaucheModel;
import com.gestionrh.sirh.model.TypeReponseModel;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;
import com.gestionrh.sirh.service.dto.EmbaucheDTO;

class RecrutementAdapterTest {

    private SelectionService selections;
    private CandidatRepository candidats;
    private ContratService contrats;
    private RecrutementAdapter adapter;
    private EmployeModel auteur;

    @BeforeEach
    void preparer() {
        selections = mock(SelectionService.class);
        candidats = mock(CandidatRepository.class);
        contrats = mock(ContratService.class);
        adapter = new RecrutementAdapter(selections, candidats, contrats);
        auteur = new EmployeModel();
        auteur.setId(3);
    }

    @Test
    void transfereUnRecrutementConfirmeEtConstruitLeDto() {
        RecrutementModel recrutement = recrutement("A_TRANSMETTRE", "CONFIRME");
        SelectionModel selection = selection();
        PropositionModel proposition = proposition();
        ReponseCandidatModel reponse = reponse();
        Candidat candidat = candidat();
        ContratModel contrat = new ContratModel();
        contrat.setReference("CTR-2026-0007");

        when(selections.recrutement(5)).thenReturn(recrutement);
        when(selections.selection(5)).thenReturn(selection);
        when(selections.propositions(5)).thenReturn(List.of(proposition));
        when(selections.reponses(9)).thenReturn(List.of(reponse));
        when(candidats.findById(71)).thenReturn(Optional.of(candidat));
        when(contrats.creerDepuisEmbauche(any(EmbaucheDTO.class), eq(auteur))).thenReturn(contrat);

        var resultat = adapter.transmettre(5, 4, 2, auteur);

        assertThat(resultat.reference()).isEqualTo("CTR-2026-0007");
        assertThat(resultat.dejaTransmis()).isFalse();
        ArgumentCaptor<EmbaucheDTO> captor = ArgumentCaptor.forClass(EmbaucheDTO.class);
        verify(contrats).creerDepuisEmbauche(captor.capture(), eq(auteur));
        EmbaucheDTO dto = captor.getValue();
        assertThat(dto.getIdRecrutement()).isEqualTo(3);
        assertThat(dto.getIdSelection()).isEqualTo(5);
        assertThat(dto.getIdCandidatB2()).isEqualTo(71);
        assertThat(dto.getNom()).isEqualTo("Rakoto");
        assertThat(dto.getPrenom()).isEqualTo("Fara");
        assertThat(dto.getCodeTypeContrat()).isEqualTo("CDI");
        assertThat(dto.getIdPoste()).isEqualTo(4);
        assertThat(dto.getIdDepartement()).isEqualTo(2);
        verify(selections).enregistrerTransfert(5, true, "CTR-2026-0007", null, 3);
    }

    @Test
    void neRenvoiePasUnDossierDejaTransmis() {
        RecrutementModel recrutement = recrutement("TRANSMIS", "TRANSMIS_BLOC4");
        recrutement.setReferenceBloc4("CTR-2026-0001");
        when(selections.recrutement(5)).thenReturn(recrutement);

        var resultat = adapter.transmettre(5, 4, 2, auteur);

        assertThat(resultat.dejaTransmis()).isTrue();
        assertThat(resultat.reference()).isEqualTo("CTR-2026-0001");
        verifyNoInteractions(candidats, contrats);
        verify(selections, never()).enregistrerTransfert(any(), anyBoolean(), any(), any(), any());
    }

    @Test
    void enregistreLEchecLorsqueLeBloc4Refuse() {
        RecrutementModel recrutement = recrutement("A_TRANSMETTRE", "CONFIRME");
        SelectionModel selection = selection();
        PropositionModel proposition = proposition();
        ReponseCandidatModel reponse = reponse();
        Candidat candidat = candidat();
        when(selections.recrutement(5)).thenReturn(recrutement);
        when(selections.selection(5)).thenReturn(selection);
        when(selections.propositions(5)).thenReturn(List.of(proposition));
        when(selections.reponses(9)).thenReturn(List.of(reponse));
        when(candidats.findById(71)).thenReturn(Optional.of(candidat));
        when(contrats.creerDepuisEmbauche(any(EmbaucheDTO.class), eq(auteur)))
                .thenThrow(new Bloc4Exception("Département et poste requis pour l'embauche."));

        assertThatThrownBy(() -> adapter.transmettre(5, null, null, auteur))
                .isInstanceOf(Bloc3Exception.class)
                .hasMessageContaining("Département et poste requis");

        verify(selections).enregistrerTransfert(5, false, null,
                "Département et poste requis pour l'embauche.", 3);
    }

    private RecrutementModel recrutement(String etatTransfert, String etatRecrutement) {
        RecrutementModel recrutement = new RecrutementModel();
        ReflectionTestUtils.setField(recrutement, "idRecrutement", 3);
        recrutement.setNombrePostes(1);
        recrutement.setConfirmeLe(LocalDate.of(2026, 10, 21));
        EtatTransfertModel transfert = mock(EtatTransfertModel.class);
        when(transfert.getCode()).thenReturn(etatTransfert);
        recrutement.setEtatTransfert(transfert);
        EtatRecrutementModel etat = mock(EtatRecrutementModel.class);
        when(etat.getCode()).thenReturn(etatRecrutement);
        recrutement.setEtatRecrutement(etat);
        return recrutement;
    }

    private SelectionModel selection() {
        SelectionModel selection = new SelectionModel();
        ReflectionTestUtils.setField(selection, "idSelection", 5);
        selection.setIdCandidatureB2(120);
        selection.setIdOffreB2(88);
        selection.setIdCandidatB2(71);
        return selection;
    }

    private PropositionModel proposition() {
        PropositionModel proposition = new PropositionModel();
        ReflectionTestUtils.setField(proposition, "idProposition", 9);
        proposition.setNumeroVersion(1);
        proposition.setIntitulePoste("Développeur");
        proposition.setSalaireMensuel(new BigDecimal("2500000"));
        proposition.setDateDebutPrevue(LocalDate.of(2026, 11, 2));
        proposition.setDureeMois(12);
        TypeContratEmbaucheModel type = mock(TypeContratEmbaucheModel.class);
        when(type.getCode()).thenReturn("CDI");
        proposition.setTypeContrat(type);
        EtatPropositionModel etat = mock(EtatPropositionModel.class);
        when(etat.getCode()).thenReturn("ACCEPTEE");
        proposition.setEtatProposition(etat);
        return proposition;
    }

    private ReponseCandidatModel reponse() {
        ReponseCandidatModel reponse = new ReponseCandidatModel();
        TypeReponseModel type = mock(TypeReponseModel.class);
        when(type.getCode()).thenReturn("ACCEPTEE");
        reponse.setTypeReponse(type);
        return reponse;
    }

    private Candidat candidat() {
        Candidat candidat = new Candidat();
        candidat.setIdCandidat(71);
        candidat.setNom("Rakoto");
        candidat.setPrenom("Fara");
        candidat.setEmail("fara.rakoto@example.mg");
        candidat.setTelephone("0340000000");
        return candidat;
    }
}
