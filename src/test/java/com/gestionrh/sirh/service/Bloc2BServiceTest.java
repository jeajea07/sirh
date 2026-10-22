package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.gestionrh.sirh.model.CandidatModel;
import com.gestionrh.sirh.model.CandidatureModel;
import com.gestionrh.sirh.model.ReponseTestTechniqueModel;
import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.repository.CandidatRepository;
import com.gestionrh.sirh.repository.CandidatureRepository;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;

@ExtendWith(MockitoExtension.class)
class Bloc2BServiceTest {

    @Mock
    private CandidatureRepository candidatures;

    @Mock
    private TestTechniqueRepository tests;

    @Mock
    private ReponseTestTechniqueRepository reponses;

    @Mock
    private CandidatRepository candidats;

    @InjectMocks
    private Bloc2BService service;

    @Test
    void creerAccesCandidatGenereIdentifiantEtMotDePasseTemporaire() {
        CandidatureModel candidature = new CandidatureModel();
        candidature.setIdCandidature(12L);
        candidature.setStatut("DOSSIER_VALIDE");
        CandidatModel candidat = new CandidatModel();
        candidat.setIdCandidat(5L);
        candidat.setEmail("jean@test.fr");
        candidature.setCandidat(candidat);

        when(candidatures.findById(12L)).thenReturn(Optional.of(candidature));

        Bloc2BService serviceWithEncoder = new Bloc2BService(candidatures, candidats, tests, reponses, new BCryptPasswordEncoder());
        Bloc2BService.AccesCandidat acces = serviceWithEncoder.creerAccesCandidat(12L);

        assertThat(acces.identifiant()).startsWith("CAND");
        assertThat(acces.motDePasseTemporaire()).isNotBlank();
        assertThat(candidature.isAccesCree()).isTrue();
        assertThat(candidat.getMotDePasse()).isNotBlank();
    }

    @Test
    void relancerTestTechniqueConserveLesReponsesExistantes() {
        TestTechniqueModel test = new TestTechniqueModel();
        test.setIdTestTechnique(7L);
        test.setStatut("EN_COURS");
        test.setNoteGenerale(BigDecimal.ZERO);

        ReponseTestTechniqueModel reponse = new ReponseTestTechniqueModel();
        reponse.setIdReponse(1L);
        reponse.setQuestionRef("Q1");
        reponse.setTypeQuestion("CHOIX");
        reponse.setReponseTextuelle("Oui");
        reponse.setTestTechnique(test);
        test.getReponses().add(reponse);

        when(tests.findById(7L)).thenReturn(Optional.of(test));

        Bloc2BService serviceWithEncoder = new Bloc2BService(candidatures, candidats, tests, reponses, new BCryptPasswordEncoder());
        serviceWithEncoder.relancerTestTechnique(7L);

        assertThat(test.getStatut()).isEqualTo("RELANCE");
        assertThat(test.getReponses()).hasSize(1);
    }

    @Test
    void confirmerCandidatsAdmisAppliqueLeSeuil() {
        CandidatureModel candidature = new CandidatureModel();
        candidature.setIdCandidature(21L);
        candidature.setStatut("TEST_OK");

        TestTechniqueModel test = new TestTechniqueModel();
        test.setIdTestTechnique(4L);
        test.setCandidature(candidature);
        test.setSeuilNote(new BigDecimal("10.0"));
        test.setNoteGenerale(new BigDecimal("12.5"));
        test.setStatut("ADMIS");
        candidature.setTestTechnique(test);

        when(candidatures.findById(21L)).thenReturn(Optional.of(candidature));
        when(tests.findByCandidature(candidature)).thenReturn(Optional.of(test));

        Bloc2BService serviceWithEncoder = new Bloc2BService(candidatures, candidats, tests, reponses, new BCryptPasswordEncoder());
        Bloc2BService.Bloc2BResultat resultat = serviceWithEncoder.confirmerCandidatureAdmise(21L, new BigDecimal("10.0"));

        assertThat(resultat.admis()).isTrue();
        assertThat(candidature.getStatut()).isEqualTo("ADMIS");
    }
}
