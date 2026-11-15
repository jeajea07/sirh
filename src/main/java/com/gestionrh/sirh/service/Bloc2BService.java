package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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

        // Seuls les candidats dont le dossier est validé sont convoqués au test (le statut n'est pas modifié).
        if (candidature.getStatut() != CandidatureStatut.DOSSIER_VALIDE
                && candidature.getStatut() != CandidatureStatut.TEST_TECHNIQUE) {
            throw new IllegalStateException("L'accès au test n'est possible que pour un dossier validé (statut actuel : "
                    + candidature.getStatut() + ").");
        }

        String identifiant = "CAND" + candidat.getIdCandidat();
        String motDePasseTemporaire = UUID.randomUUID().toString().substring(0, 8);

        candidat.setIdentifiant(identifiant);
        candidat.setMotDePasse(passwordEncoder.encode(motDePasseTemporaire));
        candidat.setAccesCree(true);
        candidat.setActif(true);
        candidats.save(candidat);

        candidature.setAccesCree(true);
        candidatures.save(candidature);

        ouvrirTest(candidature, seuil);
        return new AccesCandidat(identifiant, motDePasseTemporaire);
    }

    /** Durée de la séance unique de test (en secondes). */
    public static final int DUREE_TEST_SECONDES = 1800;

    /**
     * Ouvre la session de test si elle n'existe pas encore. Elle reste « programmée » (sans chronomètre) :
     * l'accès est créé dès la validation du dossier, le candidat passe le test plus tard dans l'entreprise.
     */
    @Transactional
    public TestTechniqueModel ouvrirTest(Candidature candidature, BigDecimal seuil) {
        return tests.findByCandidature(candidature).orElseGet(() -> {
            TestTechniqueModel test = new TestTechniqueModel();
            test.setCandidature(candidature);
            test.setStatut(Bloc2BStatus.TEST_PROGRAMME);
            test.setSeuilNote(seuil == null ? BigDecimal.ZERO : seuil);
            test.setDateDebut(LocalDateTime.now());
            test.setDureeSecondes(null);
            test.setDateFin(null);
            return tests.save(test);
        });
    }

    /** Démarre le chronomètre de la séance unique quand le candidat ouvre son test pour la première fois. */
    @Transactional
    public void demarrerTestSiProgramme(Integer idCandidature) {
        tests.findByCandidature_IdCandidature(idCandidature)
                .filter(t -> Bloc2BStatus.TEST_PROGRAMME.equals(t.getStatut()))
                .ifPresent(t -> {
                    t.setStatut(Bloc2BStatus.TEST_EN_COURS);
                    t.setDateDebut(LocalDateTime.now());
                    t.setDureeSecondes(DUREE_TEST_SECONDES);
                    t.setDateFin(t.getDateDebut().plusSeconds(DUREE_TEST_SECONDES));
                    tests.save(t);
                });
    }

    /** Enregistre les reponses du candidat, corrige le test et met a jour la candidature. */
    @Transactional
    public ResultatTest soumettreTest(Integer idCandidature, Map<String, String> reponsesCandidat) {
        Candidature candidature = chargerCandidature(idCandidature);
        TestTechniqueModel test = tests.findByCandidature(candidature)
                .orElseGet(() -> ouvrirTest(candidature, BigDecimal.ZERO));

        if (test.estExpiree()) {
            test.marquerAbsence("Temps du test écoulé");
            test.setDateFin(LocalDateTime.now());
            tests.save(test);
            throw new IllegalStateException("Le temps imparti pour le test est écoulé.");
        }

        // Séance unique : un test déjà soumis/corrigé ne peut pas être resoumis (double clic, retour arrière…).
        String statut = test.getStatut();
        if (Bloc2BStatus.TEST_ADMIS.equals(statut) || Bloc2BStatus.TEST_REFUSE.equals(statut)
                || Bloc2BStatus.TEST_CORRIGE.equals(statut) || Bloc2BStatus.TEST_ABSENT.equals(statut)) {
            throw new IllegalStateException("Ce test a déjà été soumis : il ne peut être passé qu'une seule fois.");
        }

        // Hibernate exécute les INSERT (id IDENTITY) avant les DELETE en attente : sans flush, les anciennes
        // réponses sont encore en base et la contrainte unique (id_test_technique, question_ref) saute.
        reponses.deleteAllInBatch(reponses.findByTestTechniqueOrderByIdReponseAsc(test));
        reponses.flush();
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
    public ResultatTest corrigerTestTechnique(Integer idCandidature, Map<String, String> corrections, String commentaire) {
        Candidature candidature = chargerCandidature(idCandidature);
        TestTechniqueModel test = tests.findByCandidature(candidature)
                .orElseThrow(() -> new IllegalArgumentException("Aucun test technique n’a été ouvert pour cette candidature."));

        if (test.estExpiree()) {
            test.marquerAbsence("Temps du test écoulé");
            tests.save(test);
            throw new IllegalStateException("Le test n’est plus corrigeable : la session est expirée.");
        }

        BigDecimal total = BigDecimal.ZERO;
        List<ReponseTestTechniqueModel> reponsesTest = reponses.findByTestTechniqueOrderByIdReponseAsc(test);
        for (ReponseTestTechniqueModel reponse : reponsesTest) {
            String correction = corrections == null ? null : corrections.get(reponse.getQuestionRef());
            if (correction == null || correction.isBlank()) {
                continue;
            }
            BigDecimal note = normaliserNote(correction);
            reponse.setNote(note);
            reponse.setCorrect(note.compareTo(BigDecimal.ZERO) > 0);
            reponse.setCorrigee(true);
            reponse.setCorrectionManuelle(true);
            reponse.setCommentaireCorrecteur(commentaire);
            reponses.save(reponse);
            total = total.add(note);
        }

        if (reponsesTest.isEmpty()) {
            total = test.getNoteGenerale() == null ? BigDecimal.ZERO : test.getNoteGenerale();
        }

        BigDecimal seuil = test.getSeuilNote() == null ? BigDecimal.ZERO : test.getSeuilNote();
        test.setNoteGenerale(total);
        test.setCorrectionManuelle(true);
        test.setStatut(Bloc2BStatus.TEST_CORRIGE);
        test.setDateFin(LocalDateTime.now());
        tests.save(test);

        candidature.setNoteFinale(total);
        candidature.setStatut(CandidatureStatut.TEST_TECHNIQUE);
        candidatures.save(candidature);

        return new ResultatTest(total.compareTo(seuil) >= 0, total, seuil);
    }

    @Transactional
    public void relancerTestTechnique(Integer idCandidature) {
        Candidature candidature = chargerCandidature(idCandidature);
        TestTechniqueModel test = tests.findByCandidature(candidature)
                .orElseThrow(() -> new IllegalArgumentException("Aucun test technique pour la candidature " + idCandidature));
        test.setRelance(true);
        test.setStatut(Bloc2BStatus.TEST_RELANCE);
        test.setDateDebut(LocalDateTime.now());
        test.setDateFin(test.getDateDebut().plusSeconds(1800));
        tests.save(test);
    }

    @Transactional
    public void programmerEntretien(Integer idCandidature, String niveau) {
        Candidature candidature = chargerCandidature(idCandidature);
        candidature.setStatut(statutEntretien(niveau, candidature.getStatut(), true));
        candidatures.save(candidature);
    }

    @Transactional
    public void planifierEntretien(Integer idCandidature, String niveau) {
        programmerEntretien(idCandidature, niveau);
    }

    @Transactional
    public void validerEntretien(Integer idCandidature, String niveau, boolean admis) {
        Candidature candidature = chargerCandidature(idCandidature);
        candidature.setStatut(statutEntretien(niveau, candidature.getStatut(), admis));
        candidatures.save(candidature);
    }

    @Transactional
    public void confirmerFinale(Integer idCandidature, boolean admis) {
        Candidature candidature = chargerCandidature(idCandidature);
        CandidatureStatut statutAvant = candidature.getStatut();
        if (statutAvant != CandidatureStatut.ENTRETIEN_DG && statutAvant != CandidatureStatut.CONFIRMATION_FINALE) {
            throw new IllegalStateException("La confirmation finale ne peut être faite qu’après le niveau DG ou une confirmation déjà initiée.");
        }
        candidature.setStatut(admis ? CandidatureStatut.CONFIRMATION_FINALE : CandidatureStatut.NON_RETENUE);
        candidatures.save(candidature);
    }

    @Transactional
    public void noterEntretien(Integer idCandidature, String niveau, BigDecimal note, boolean admis) {
        Candidature candidature = chargerCandidature(idCandidature);
        candidature.setNoteFinale(note == null ? candidature.getNoteFinale() : note);
        candidature.setStatut(statutEntretien(niveau, candidature.getStatut(), admis));
        candidatures.save(candidature);
    }

    @Transactional
    public void marquerAbsence(Integer idCandidature, String motif, boolean exception) {
        Candidature candidature = chargerCandidature(idCandidature);
        TestTechniqueModel test = tests.findByCandidature(candidature)
                .orElseGet(() -> ouvrirTest(candidature, BigDecimal.ZERO));
        String motifFinal = motif == null || motif.isBlank() ? (exception ? "Exception RH" : "Absence non justifiée") : motif;
        test.setMotifAbsence(motifFinal);
        if (exception) {
            test.setMotifException(motifFinal);
            test.setStatut(Bloc2BStatus.TEST_RELANCE);
            test.setRelance(true);
            test.setDateDebut(LocalDateTime.now());
            test.setDateFin(test.getDateDebut().plusSeconds(1800));
        } else {
            test.marquerAbsence(motifFinal);
            candidature.setStatut(CandidatureStatut.NON_RETENUE);
        }
        tests.save(test);
        candidatures.save(candidature);
    }

    /** Decision RH apres le test : le candidat passe a l'entretien 1 (RH) ou est ecarte. */
    @Transactional
    public void decider(Integer idCandidature, boolean admis) {
        Candidature candidature = chargerCandidature(idCandidature);
        if (candidature.getStatut() != CandidatureStatut.DOSSIER_VALIDE
                && candidature.getStatut() != CandidatureStatut.TEST_TECHNIQUE) {
            throw new IllegalStateException("La décision du test a déjà été prise (statut actuel : "
                    + candidature.getStatut() + ").");
        }
        TestTechniqueModel test = tests.findByCandidature(candidature).orElse(null);
        boolean retenu = admis;
        if (test != null && test.isAbsent() && test.getMotifException() != null && !test.getMotifException().isBlank() && admis) {
            test.setStatut(Bloc2BStatus.TEST_ADMIS);
            test.setDateFin(LocalDateTime.now());
            tests.save(test);
            candidature.setNoteFinale(test.getNoteGenerale() == null ? BigDecimal.ZERO : test.getNoteGenerale());
            candidature.setStatut(CandidatureStatut.ENTRETIEN_RH);
            candidatures.save(candidature);
            return;
        }
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
            candidature.setNoteFinale(test.getNoteGenerale() == null ? BigDecimal.ZERO : test.getNoteGenerale());
        }
        // Bloc 2 : un admis au test est convoqué à l'entretien 1 (RH) ; il n'atteint la confirmation
        // finale qu'après les trois niveaux d'entretien.
        candidature.setStatut(retenu ? CandidatureStatut.ENTRETIEN_RH : CandidatureStatut.NON_RETENUE);
        candidatures.save(candidature);
    }

    private CandidatureStatut statutEntretien(String niveau, CandidatureStatut statutCourant, boolean admis) {
        String niveauNet = niveau == null ? "" : niveau.trim().toUpperCase(Locale.ROOT);
        if ("RH".equals(niveauNet)) {
            if (statutCourant == CandidatureStatut.CONFIRMATION_FINALE || statutCourant == null) {
                return admis ? CandidatureStatut.ENTRETIEN_RH : CandidatureStatut.NON_RETENUE;
            }
            if (statutCourant == CandidatureStatut.ENTRETIEN_RH) {
                return admis ? CandidatureStatut.ENTRETIEN_TECHNICIEN : CandidatureStatut.NON_RETENUE;
            }
            return admis ? CandidatureStatut.ENTRETIEN_RH : CandidatureStatut.NON_RETENUE;
        }
        if ("TECHNICIEN".equals(niveauNet) || "TECHNIQUE".equals(niveauNet)) {
            if (statutCourant == CandidatureStatut.ENTRETIEN_RH || statutCourant == CandidatureStatut.ENTRETIEN_TECHNICIEN) {
                return admis ? CandidatureStatut.ENTRETIEN_DG : CandidatureStatut.NON_RETENUE;
            }
            if (statutCourant == CandidatureStatut.ENTRETIEN_DG) {
                return admis ? CandidatureStatut.CONFIRMATION_FINALE : CandidatureStatut.NON_RETENUE;
            }
            return admis ? CandidatureStatut.ENTRETIEN_TECHNICIEN : CandidatureStatut.NON_RETENUE;
        }
        if ("DG".equals(niveauNet)) {
            if (statutCourant == CandidatureStatut.ENTRETIEN_DG || statutCourant == CandidatureStatut.ENTRETIEN_TECHNICIEN) {
                return admis ? CandidatureStatut.CONFIRMATION_FINALE : CandidatureStatut.NON_RETENUE;
            }
            return admis ? CandidatureStatut.ENTRETIEN_DG : CandidatureStatut.NON_RETENUE;
        }
        throw new IllegalArgumentException("Niveau d’entretien inconnu : " + niveau);
    }

    private BigDecimal normaliserNote(String valeur) {
        if (valeur == null) {
            return BigDecimal.ZERO;
        }
        String normalisee = valeur.trim();
        if (normalisee.isBlank()) {
            return BigDecimal.ZERO;
        }
        String minuscule = normalisee.toLowerCase(Locale.ROOT);
        if ("oui".equals(minuscule) || "true".equals(minuscule) || "vrai".equals(minuscule) || "1".equals(minuscule)) {
            return BigDecimal.ONE;
        }
        if ("non".equals(minuscule) || "false".equals(minuscule) || "faux".equals(minuscule) || "0".equals(minuscule)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(normalisee);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private Candidature chargerCandidature(Integer idCandidature) {
        return candidatures.findById(idCandidature)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable : " + idCandidature));
    }
}
