package com.gestionrh.sirh.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import com.gestionrh.sirh.repository.Bloc2BCandidatRepository;
import com.gestionrh.sirh.repository.Bloc2BCandidatureRepository;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;
import com.gestionrh.sirh.repository.UtilisateurRepository;
import com.gestionrh.sirh.security.CompteService;

@ExtendWith(MockitoExtension.class)
class Bloc2BServiceTest {

    @Mock
    private Bloc2BCandidatureRepository candidatures;

    @Mock
    private TestTechniqueRepository tests;

    @Mock
    private ReponseTestTechniqueRepository reponses;

    @Mock
    private Bloc2BCandidatRepository candidats;

    @Mock
    private UtilisateurRepository utilisateurs;

    @InjectMocks
    private Bloc2BService service;

    @Test
    void creerAccesCandidatGenereIdentifiantEtMotDePasseTemporaire() {
        CandidatureModel candidature = new CandidatureModel();
        candidature.setIdCandidature(12L);
        candidature.setStatut(Bloc2BStatus.CANDIDATURE_DOSSIER_VALIDE);
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
        test.setStatut(Bloc2BStatus.TEST_EN_COURS);
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

        assertThat(test.getStatut()).isEqualTo(Bloc2BStatus.TEST_RELANCE);
        assertThat(test.getReponses()).hasSize(1);
    }

    @Test
    void confirmerCandidatsAdmisAppliqueLeSeuil() {
        CandidatureModel candidature = new CandidatureModel();
        candidature.setIdCandidature(21L);
        candidature.setStatut(Bloc2BStatus.CANDIDATURE_TEST_OK);

        TestTechniqueModel test = new TestTechniqueModel();
        test.setIdTestTechnique(4L);
        test.setCandidature(candidature);
        test.setSeuilNote(Bloc2BService.SEUIL_PAR_DEFAUT);
        test.setNoteGenerale(new BigDecimal("12.5"));
        test.setStatut(Bloc2BStatus.TEST_ADMIS);
        candidature.setTestTechnique(test);

        when(candidatures.findById(21L)).thenReturn(Optional.of(candidature));
        when(tests.findByCandidature(candidature)).thenReturn(Optional.of(test));

        Bloc2BService serviceWithEncoder = new Bloc2BService(candidatures, candidats, tests, reponses, new BCryptPasswordEncoder());
        Bloc2BService.Bloc2BResultat resultat = serviceWithEncoder.confirmerCandidatureAdmise(21L, Bloc2BService.SEUIL_PAR_DEFAUT);

        assertThat(resultat.admis()).isTrue();
        assertThat(candidature.getStatut()).isEqualTo(Bloc2BStatus.CANDIDATURE_ADMIS);
    }

    @Test
    void compteServiceAuthentifieUnCandidatParEmail() {
        CandidatModel candidat = new CandidatModel();
        candidat.setEmail("candidat@test.fr");
        candidat.setMotDePasse(new BCryptPasswordEncoder().encode("secret"));
        candidat.setActif(true);

        when(candidats.findByEmail("candidat@test.fr")).thenReturn(Optional.of(candidat));

        CompteService compteService = new CompteService(utilisateurs, candidats);

        var user = compteService.loadUserByUsername("candidat@test.fr");

        assertThat(user.getUsername()).isEqualTo("candidat@test.fr");
        assertThat(user.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_CANDIDAT"));
    }
}
