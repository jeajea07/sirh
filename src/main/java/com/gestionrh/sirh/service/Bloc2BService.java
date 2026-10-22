package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.CandidatModel;
import com.gestionrh.sirh.model.CandidatureModel;
import com.gestionrh.sirh.model.ReponseTestTechniqueModel;
import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.repository.CandidatRepository;
import com.gestionrh.sirh.repository.CandidatureRepository;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;

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

    @Transactional
    public AccesCandidat creerAccesCandidat(Long idCandidature) {
        CandidatureModel candidature = candidatures.findById(idCandidature)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable : " + idCandidature));
        CandidatModel candidat = candidature.getCandidat();
        if (candidat == null) {
            throw new IllegalStateException("La candidature n'a pas de candidat associé.");
        }

        String identifiant = "CAND" + candidat.getIdCandidat();
        String motDePasseTemporaire = UUID.randomUUID().toString().substring(0, 8);

        candidat.setIdentifiant(identifiant);
        candidat.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));
        candidat.setAccesCree(true);
        candidature.setAccesCree(true);
        candidature.setDossierValide(true);
        candidature.setStatut("DOSSIER_VALIDE");

        candidats.save(candidat);
        candidatures.save(candidature);

        return new AccesCandidat(identifiant, motDePasseTemporaire);
    }

    @Transactional
    public void relancerTestTechnique(Long idTestTechnique) {
        TestTechniqueModel test = tests.findById(idTestTechnique)
                .orElseThrow(() -> new IllegalArgumentException("Test technique introuvable : " + idTestTechnique));

        test.setStatut("RELANCE");
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
        test.setStatut("CORRIGE");
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
        candidature.setStatut(admis ? "ADMIS" : "REFUSE");
        candidatures.save(candidature);

        if (test != null) {
            test.setStatut(admis ? "ADMIS" : "REFUSE");
            test.setSeuilNote(seuil == null ? BigDecimal.ZERO : seuil);
            tests.save(test);
        }

        return new Bloc2BResultat(admis, note, seuil == null ? BigDecimal.ZERO : seuil);
    }

    public record AccesCandidat(String identifiant, String motDePasseTemporaire) {
    }

    public record Bloc2BResultat(boolean admis, BigDecimal note, BigDecimal seuil) {
    }
}
