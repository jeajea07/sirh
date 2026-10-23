package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.CandidatModel;
import com.gestionrh.sirh.model.CandidatureModel;
import com.gestionrh.sirh.model.ReponseTestTechniqueModel;
import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.repository.Bloc2BCandidatRepository;
import com.gestionrh.sirh.repository.Bloc2BCandidatureRepository;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;

@Service
public class Bloc2BService {

    public static final BigDecimal SEUIL_PAR_DEFAUT = new BigDecimal("10.00");

    private final Bloc2BCandidatureRepository candidatures;
    private final Bloc2BCandidatRepository candidats;
    private final TestTechniqueRepository tests;
    private final ReponseTestTechniqueRepository reponses;
    private final PasswordEncoder passwordEncoder;

    public Bloc2BService(Bloc2BCandidatureRepository candidatures,
                         Bloc2BCandidatRepository candidats,
                         TestTechniqueRepository tests,
                         ReponseTestTechniqueRepository reponses,
                         PasswordEncoder passwordEncoder) {
        this.candidatures = candidatures;
        this.candidats = candidats;
        this.tests = tests;
        this.reponses = reponses;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AccesCandidat creerAccesCandidat(Long idCandidature) {
        CandidatureModel candidature = candidatures.findById(idCandidature)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable : " + idCandidature));
        CandidatModel candidat = candidature.getCandidat();
        if (candidat == null) {
            throw new IllegalStateException("La candidature n'a pas de candidat associé.");
        }
        if (candidat.isAccesCree()) {
            return new AccesCandidat(candidat.getIdentifiant(), null);
        }

        String identifiant = "CAND" + candidat.getIdCandidat();
        String motDePasseTemporaire = UUID.randomUUID().toString().substring(0, 8);

        candidat.setIdentifiant(identifiant);
        candidat.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));
        candidat.setAccesCree(true);
        candidature.setAccesCree(true);
        candidature.setDossierValide(true);
        candidature.setStatut(Bloc2BStatus.CANDIDATURE_DOSSIER_VALIDE);

        candidats.save(candidat);
        candidatures.save(candidature);

        return new AccesCandidat(identifiant, motDePasseTemporaire);
    }

    @Transactional
    public void relancerTestTechnique(Long idTestTechnique) {
        TestTechniqueModel test = tests.findById(idTestTechnique)
                .orElseThrow(() -> new IllegalArgumentException("Test technique introuvable : " + idTestTechnique));

        test.setStatut(Bloc2BStatus.TEST_RELANCE);
        test.setRelance(true);
        tests.save(test);
    }

    @Transactional
    public void corrigerTestTechnique(Long idTestTechnique) {
        TestTechniqueModel test = tests.findById(idTestTechnique)
                .orElseThrow(() -> new IllegalArgumentException("Test technique introuvable : " + idTestTechnique));

        List<ReponseTestTechniqueModel> reponsesDuTest = reponses.findByTestTechniqueOrderByIdReponseAsc(test);
        BigDecimal note = BigDecimal.ZERO;

        for (ReponseTestTechniqueModel reponse : reponsesDuTest) {
            if ("CHOIX".equalsIgnoreCase(reponse.getTypeQuestion()) || "QCM".equalsIgnoreCase(reponse.getTypeQuestion())) {
                boolean correct = reponse.getReponseTextuelle() != null && !reponse.getReponseTextuelle().isBlank();
                reponse.setCorrigee(true);
                reponse.setCorrect(correct);
                if (correct) {
                    note = note.add(BigDecimal.ONE);
                }
                reponse.setNote(correct ? BigDecimal.ONE : BigDecimal.ZERO);
            }
        }

        test.setNoteGenerale(note);
        test.setStatut(Bloc2BStatus.TEST_CORRIGE);
        tests.save(test);
        reponses.saveAll(reponsesDuTest);
    }

    @Transactional
    public Bloc2BResultat confirmerCandidatureAdmise(Long idCandidature, BigDecimal seuil) {
        CandidatureModel candidature = candidatures.findById(idCandidature)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable : " + idCandidature));

        TestTechniqueModel test = tests.findByCandidature(candidature).orElse(null);
        BigDecimal note = test == null || test.getNoteGenerale() == null ? BigDecimal.ZERO : test.getNoteGenerale();
        boolean admis = note.compareTo(seuil == null ? BigDecimal.ZERO : seuil) >= 0;

        candidature.setNoteFinale(note.doubleValue());
        candidature.setStatut(admis ? Bloc2BStatus.CANDIDATURE_ADMIS : Bloc2BStatus.CANDIDATURE_REFUSE);
        candidatures.save(candidature);

        if (test != null) {
            test.setStatut(admis ? Bloc2BStatus.TEST_ADMIS : Bloc2BStatus.TEST_REFUSE);
            test.setSeuilNote(seuil == null ? BigDecimal.ZERO : seuil);
            tests.save(test);
        }

        return new Bloc2BResultat(admis, note, seuil == null ? BigDecimal.ZERO : seuil);
    }

    @Transactional
    public void soumettreTestTechnique(Long idCandidature, Map<String, String> reponsesSoumises) {
        CandidatureModel candidature = candidatures.findById(idCandidature)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable : " + idCandidature));

        TestTechniqueModel test = tests.findByCandidature(candidature)
                .orElseGet(() -> {
                    TestTechniqueModel nouveau = new TestTechniqueModel();
                    nouveau.setCandidature(candidature);
                    nouveau.setStatut(Bloc2BStatus.TEST_EN_COURS);
                    nouveau.setSeuilNote(SEUIL_PAR_DEFAUT);
                    return nouveau;
                });

        List<ReponseTestTechniqueModel> reponsesExistantes = reponses.findByTestTechniqueOrderByIdReponseAsc(test);
        List<ReponseTestTechniqueModel> resultat = reponsesExistantes.stream()
                .filter(rep -> rep.getQuestionRef() != null)
                .toList();

        BigDecimal note = BigDecimal.ZERO;
        for (Bloc2BQuestion question : Bloc2BQuestion.QUESTIONS) {
            String reference = question.reference();
            String valeur = reponsesSoumises.getOrDefault(reference, "").trim();
            ReponseTestTechniqueModel reponse = resultat.stream()
                    .filter(r -> reference.equals(r.getQuestionRef()))
                    .findFirst()
                    .orElseGet(() -> {
                        ReponseTestTechniqueModel n = new ReponseTestTechniqueModel();
                        n.setTestTechnique(test);
                        n.setQuestionRef(reference);
                        n.setTypeQuestion(question.type());
                        return n;
                    });

            reponse.setReponseTextuelle(valeur);
            reponse.setQuestionRef(reference);
            reponse.setTypeQuestion(question.type());
            reponse.setCorrigee(true);

            boolean correct = estCorrect(question, valeur);
            reponse.setCorrect(correct);
            reponse.setNote(correct ? BigDecimal.ONE : BigDecimal.ZERO);
            if (correct) {
                note = note.add(BigDecimal.ONE);
            }

            if (!resultat.contains(reponse)) {
                test.getReponses().add(reponse);
            }
        }

        test.setNoteGenerale(note);
        test.setDateFin(LocalDateTime.now());
        test.setStatut(Bloc2BStatus.TEST_CORRIGE);
        candidature.setStatut(Bloc2BStatus.CANDIDATURE_TEST_OK);
        candidature.setNoteFinale(note.doubleValue());

        tests.save(test);
        reponses.saveAll(test.getReponses());
        candidatures.save(candidature);
    }

    private boolean estCorrect(Bloc2BQuestion question, String valeur) {
        return question != null && question.matches(valeur);
    }

    public record AccesCandidat(String identifiant, String motDePasseTemporaire) {
    }

    public record Bloc2BResultat(boolean admis, BigDecimal note, BigDecimal seuil) {
    }
}
