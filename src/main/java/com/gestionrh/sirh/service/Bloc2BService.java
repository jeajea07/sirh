package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.model.ReponseTestTechniqueModel;
import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.model.recrutement.Candidat;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;

/**
 * Bloc 2B adapte au socle 2A : compte candidat, test technique et decision.
 * Les candidatures et les candidats restent ceux du Bloc 2A ; seules les tables
 * test_technique et reponse_test_technique sont ajoutees.
 */
@Service
public class Bloc2BService {

    private final CandidatureRepository candidatures;
    private final CandidatRepository candidats;
    private final TestTechniqueRepository tests;
    private final ReponseTestTechniqueRepository reponses;
    private final PasswordEncoder passwordEncoder;

    public Bloc2BService(CandidatureRepository candidatures,
                         CandidatRepository candidats,
                         TestTechniqueRepository tests,
                         ReponseTestTechniqueRepository reponses,
                         PasswordEncoder passwordEncoder) {
        this.candidatures = candidatures;
        this.candidats = candidats;
        this.tests = tests;
        this.reponses = reponses;
        this.passwordEncoder = passwordEncoder;
    }

    public record AccesCandidat(String identifiant, String motDePasseTemporaire) {
    }

    public record ResultatTest(boolean admis, BigDecimal note, BigDecimal seuil) {
    }

    /** Cree (ou renouvelle) le compte de test du candidat et ouvre la session de test. */
    @Transactional
    public AccesCandidat creerAccesCandidat(Integer idCandidature, BigDecimal seuil) {
        Candidature candidature = chargerCandidature(idCandidature);
        Candidat candidat = candidature.getCandidat();
        if (candidat == null) {
            throw new IllegalStateException("La candidature n'a pas de candidat associé.");
        }

        String identifiant = "CAND" + candidat.getIdCandidat();
        String motDePasseTemporaire = UUID.randomUUID().toString().substring(0, 8);

        candidat.setIdentifiant(identifiant);
        candidat.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));
        candidat.setAccesCree(true);
        candidat.setActif(true);
        candidats.save(candidat);

        candidature.setAccesCree(true);
        candidature.setStatut(CandidatureStatut.DOSSIER_VALIDE);
        candidatures.save(candidature);

        ouvrirTest(candidature, seuil);
        return new AccesCandidat(identifiant, motDePasseTemporaire);
    }

    /** Ouvre la session de test si elle n'existe pas encore. */
    @Transactional
    public TestTechniqueModel ouvrirTest(Candidature candidature, BigDecimal seuil) {
        return tests.findByCandidature(candidature).orElseGet(() -> {
            TestTechniqueModel test = new TestTechniqueModel();
            test.setCandidature(candidature);
            test.setStatut(Bloc2BStatus.TEST_EN_COURS);
            test.setSeuilNote(seuil == null ? BigDecimal.ZERO : seuil);
            test.setDateDebut(LocalDateTime.now());
            return tests.save(test);
        });
    }

    /** Enregistre les reponses du candidat, corrige le test et met a jour la candidature. */
    @Transactional
    public ResultatTest soumettreTest(Integer idCandidature, Map<String, String> reponsesCandidat) {
        Candidature candidature = chargerCandidature(idCandidature);
        TestTechniqueModel test = tests.findByCandidature(candidature)
                .orElseGet(() -> ouvrirTest(candidature, BigDecimal.ZERO));

        reponses.deleteAll(reponses.findByTestTechniqueOrderByIdReponseAsc(test));
        BigDecimal note = BigDecimal.ZERO;
        for (Bloc2BQuestion question : Bloc2BQuestion.QUESTIONS) {
            String valeur = reponsesCandidat == null ? null : reponsesCandidat.get(question.reference());
            boolean correct = question.matches(valeur);
            ReponseTestTechniqueModel reponse = new ReponseTestTechniqueModel();
            reponse.setTestTechnique(test);
            reponse.setQuestionRef(question.reference());
            reponse.setTypeQuestion(question.type());
            reponse.setReponseTextuelle(valeur);
            reponse.setCorrigee(true);
            reponse.setCorrect(correct);
            reponse.setNote(correct ? BigDecimal.ONE : BigDecimal.ZERO);
            reponses.save(reponse);
            if (correct) {
                note = note.add(BigDecimal.ONE);
            }
        }

        BigDecimal seuil = test.getSeuilNote() == null ? BigDecimal.ZERO : test.getSeuilNote();
        boolean admis = note.compareTo(seuil) >= 0;
        test.setNoteGenerale(note);
        test.setStatut(admis ? Bloc2BStatus.TEST_ADMIS : Bloc2BStatus.TEST_REFUSE);
        test.setDateFin(LocalDateTime.now());
        tests.save(test);

        candidature.setNoteFinale(note);
        candidature.setStatut(CandidatureStatut.TEST_TECHNIQUE);
        candidatures.save(candidature);

        return new ResultatTest(admis, note, seuil);
    }

    @Transactional
    public void relancerTestTechnique(Integer idTestTechnique) {
        TestTechniqueModel test = tests.findById(idTestTechnique)
                .orElseThrow(() -> new IllegalArgumentException("Test technique introuvable : " + idTestTechnique));
        test.setStatut(Bloc2BStatus.TEST_RELANCE);
        test.setRelance(true);
        tests.save(test);
    }

    /** Decision RH : le candidat passe en confirmation finale ou est ecarte. */
    @Transactional
    public void decider(Integer idCandidature, boolean admis) {
        Candidature candidature = chargerCandidature(idCandidature);
        TestTechniqueModel test = tests.findByCandidature(candidature).orElse(null);
        boolean retenu = admis;
        if (retenu && test != null) {
            BigDecimal seuil = test.getSeuilNote() == null ? BigDecimal.ZERO : test.getSeuilNote();
            BigDecimal note = test.getNoteGenerale() == null ? BigDecimal.ZERO : test.getNoteGenerale();
            retenu = note.compareTo(seuil) >= 0;
            if (!retenu) {
                throw new IllegalStateException("La note du test est inférieure au seuil : décision refusée impossible à admettre.");
            }
        }
        if (test != null) {
            test.setStatut(retenu ? Bloc2BStatus.TEST_ADMIS : Bloc2BStatus.TEST_REFUSE);
            tests.save(test);
            candidature.setNoteFinale(test.getNoteGenerale());
        }
        candidature.setStatut(retenu ? CandidatureStatut.CONFIRMATION_FINALE : CandidatureStatut.NON_RETENUE);
        candidatures.save(candidature);
    }

    private Candidature chargerCandidature(Integer idCandidature) {
        return candidatures.findById(idCandidature)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable : " + idCandidature));
    }
}
