package com.gestionrh.sirh.service.recrutement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.EntretienModel;
import com.gestionrh.sirh.model.EntretienNoteModel;
import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.model.recrutement.HistoriqueCandidature;
import com.gestionrh.sirh.repository.EntretienRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.HistoriqueCandidatureRepository;

/**
 * Bloc 2 — étapes 5, 6 et 7 : entretiens RH (1), Technicien (2) et DG (3), dans cet ordre.
 *
 * Règles appliquées :
 * - seuls les candidats ayant réussi le niveau précédent sont convoqués (statut de la candidature) ;
 * - grille connue à l'avance : importance 1 à 3, questions éliminatoires avec note minimale ;
 *   elle est figée dès que des notes sont saisies ;
 * - notes de 1 à 5 ; note générale = moyenne pondérée par l'importance ;
 * - proposé « admis » si note générale ≥ seuil ET minimums des questions éliminatoires respectés ;
 * - le passage au niveau suivant est toujours confirmé par la RH (motif obligatoire si elle
 *   s'écarte de la proposition) ;
 * - un report conserve le rendez-vous d'origine ; une absence est notée et proposée « non admis ».
 */
@Service
public class EntretienService {

    private static final Logger log = LoggerFactory.getLogger(EntretienService.class);
    private static final DateTimeFormatter FR = DateTimeFormatter.ofPattern("dd/MM/yyyy 'à' HH'h'mm");
    public static final Set<CandidatureStatut> STATUTS_ENTRETIEN =
            EnumSet.of(CandidatureStatut.ENTRETIEN_RH, CandidatureStatut.ENTRETIEN_TECHNICIEN, CandidatureStatut.ENTRETIEN_DG);

    /** Question de la grille proposée par défaut (modifiable avant le début du niveau). */
    public record QuestionGrille(String question, int importance, boolean eliminatoire, BigDecimal noteMinimale) { }

    /** Résumé des notes d'une candidature, utilisé par le Bloc 3 (note finale et classement). */
    public record ResumeNotes(BigDecimal test20, BigDecimal entretienRh, BigDecimal entretienTech,
            BigDecimal entretienDg, BigDecimal noteFinale) { }

    private final EntretienRepository entretiens;
    private final CandidatureRepository candidatures;
    private final HistoriqueCandidatureRepository historiques;
    private final TestTechniqueRepository tests;
    private final NotificationRecrutementService notifications;

    public EntretienService(EntretienRepository entretiens, CandidatureRepository candidatures,
            HistoriqueCandidatureRepository historiques, TestTechniqueRepository tests,
            NotificationRecrutementService notifications) {
        this.entretiens = entretiens;
        this.candidatures = candidatures;
        this.historiques = historiques;
        this.tests = tests;
        this.notifications = notifications;
    }

    // ------------------------------------------------------------------ niveaux

    public static int niveauDuStatut(CandidatureStatut statut) {
        if (statut == null) return 0;
        return switch (statut) {
            case ENTRETIEN_RH -> 1;
            case ENTRETIEN_TECHNICIEN -> 2;
            case ENTRETIEN_DG -> 3;
            default -> 0;
        };
    }

    private static CandidatureStatut statutSuivant(int niveau) {
        return switch (niveau) {
            case 1 -> CandidatureStatut.ENTRETIEN_TECHNICIEN;
            case 2 -> CandidatureStatut.ENTRETIEN_DG;
            default -> CandidatureStatut.CONFIRMATION_FINALE;
        };
    }

    public static List<QuestionGrille> grilleParDefaut(int niveau) {
        BigDecimal trois = new BigDecimal("3");
        return switch (niveau) {
            case 1 -> List.of(
                    new QuestionGrille("Présentation et parcours", 1, false, null),
                    new QuestionGrille("Motivation pour le poste", 2, false, null),
                    new QuestionGrille("Communication et savoir-être", 2, true, trois),
                    new QuestionGrille("Adéquation aux conditions du poste", 1, false, null),
                    new QuestionGrille("Disponibilité", 1, false, null));
            case 2 -> List.of(
                    new QuestionGrille("Maîtrise des compétences techniques clés", 3, true, trois),
                    new QuestionGrille("Résolution d'un cas pratique", 3, false, null),
                    new QuestionGrille("Méthodes et bonnes pratiques", 2, false, null),
                    new QuestionGrille("Expériences techniques significatives", 2, false, null),
                    new QuestionGrille("Capacité d'apprentissage", 1, false, null));
            default -> List.of(
                    new QuestionGrille("Vision et projet professionnel", 2, false, null),
                    new QuestionGrille("Adhésion aux valeurs de l'entreprise", 3, true, trois),
                    new QuestionGrille("Leadership et prise de décision", 2, false, null),
                    new QuestionGrille("Engagement à long terme", 1, false, null));
        };
    }

    // ------------------------------------------------------------------ lecture

    @Transactional(readOnly = true)
    public List<EntretienModel> entretiensDe(Integer idCandidature) {
        return entretiens.findByIdCandidatureOrderByNiveauAsc(idCandidature);
    }

    @Transactional(readOnly = true)
    public EntretienModel charger(Integer idEntretien) {
        return entretiens.findWithNotesById(idEntretien)
                .orElseThrow(() -> new RegleMetierException("Entretien introuvable."));
    }

    @Transactional(readOnly = true)
    public ResumeNotes resume(Integer idCandidature) {
        BigDecimal test20 = candidatures.findById(idCandidature)
                .flatMap(tests::findByCandidature)
                .map(TestTechniqueModel::getNoteGenerale)
                .map(n -> n.multiply(BigDecimal.valueOf(20)).divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP))
                .orElse(null);
        BigDecimal[] niveaux = new BigDecimal[4];
        for (EntretienModel e : entretiens.findByIdCandidatureOrderByNiveauAsc(idCandidature)) {
            if (EntretienModel.ADMIS.equals(e.getDecision()) && e.getNoteGenerale() != null) {
                niveaux[e.getNiveau()] = e.getNoteGenerale();
            }
        }
        // Note finale : moyenne du test et des trois entretiens, ramenés sur 5.
        List<BigDecimal> composantes = new ArrayList<>();
        if (test20 != null) composantes.add(test20.divide(BigDecimal.valueOf(4), 4, RoundingMode.HALF_UP));
        for (int i = 1; i <= 3; i++) if (niveaux[i] != null) composantes.add(niveaux[i]);
        BigDecimal finale = composantes.isEmpty() ? null
                : composantes.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                        .divide(BigDecimal.valueOf(composantes.size()), 2, RoundingMode.HALF_UP);
        return new ResumeNotes(test20, niveaux[1], niveaux[2], niveaux[3], finale);
    }

    // ------------------------------------------------------------------ planification

    @Transactional
    public EntretienModel planifier(Integer idCandidature, LocalDateTime date, String lieu, String jury,
            BigDecimal seuil, List<QuestionGrille> grille, Integer idAuteur) {
        Candidature c = candidatures.findWithCandidatAndOffreByIdCandidature(idCandidature)
                .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));
        int niveau = niveauDuStatut(c.getStatut());
        if (niveau == 0) {
            throw new RegleMetierException("Cette candidature n'est pas à l'étape des entretiens (statut " + c.getStatut() + ").");
        }
        if (entretiens.findByIdCandidatureAndNiveau(idCandidature, (short) niveau).isPresent()) {
            throw new RegleMetierException("L'entretien de ce niveau est déjà planifié.");
        }
        if (date == null || date.isBefore(LocalDateTime.now())) {
            throw new RegleMetierException("La date de l'entretien doit être dans le futur.");
        }
        if (seuil == null || seuil.compareTo(BigDecimal.ONE) < 0 || seuil.compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new RegleMetierException("Le seuil de la note générale doit être compris entre 1 et 5.");
        }
        List<QuestionGrille> questions = grille == null ? List.of()
                : grille.stream().filter(q -> q.question() != null && !q.question().isBlank()).toList();
        if (questions.isEmpty()) {
            throw new RegleMetierException("La grille d'évaluation doit contenir au moins une question.");
        }

        EntretienModel e = new EntretienModel();
        e.setIdCandidature(idCandidature);
        e.setNiveau((short) niveau);
        e.setDatePrevue(date);
        e.setDateOrigine(date);
        e.setLieu(lieu);
        e.setJury(jury);
        e.setSeuilGeneral(seuil.setScale(2, RoundingMode.HALF_UP));
        e.setIdPlanificateur(idAuteur);
        short ordre = 1;
        for (QuestionGrille q : questions) {
            if (q.importance() < 1 || q.importance() > 3) {
                throw new RegleMetierException("L'importance d'une question doit valoir 1, 2 ou 3.");
            }
            if (q.eliminatoire() && (q.noteMinimale() == null
                    || q.noteMinimale().compareTo(BigDecimal.ONE) < 0 || q.noteMinimale().compareTo(BigDecimal.valueOf(5)) > 0)) {
                throw new RegleMetierException("Une question éliminatoire doit avoir une note minimale entre 1 et 5 : « "
                        + q.question().trim() + " ».");
            }
            EntretienNoteModel n = new EntretienNoteModel();
            n.setOrdre(ordre++);
            n.setQuestion(q.question().trim());
            n.setImportance((short) q.importance());
            n.setEliminatoire(q.eliminatoire());
            n.setNoteMinimale(q.eliminatoire() ? q.noteMinimale() : null);
            e.ajouterNote(n);
        }
        e = entretiens.save(e);
        historiser(c, "ENTRETIEN_" + niveau + "_PLANIFIE", c.getStatut(), c.getStatut(), idAuteur,
                EntretienModel.libelleNiveau(niveau) + " planifié le " + date.format(FR));
        notifierCandidat(c, "CONVOCATION_ENTRETIEN_" + niveau, "Convocation : " + EntretienModel.libelleNiveau(niveau),
                "Bonjour, vous êtes convoqué(e) à l'" + minuscule(EntretienModel.libelleNiveau(niveau))
                        + " le " + date.format(FR) + (lieu == null || lieu.isBlank() ? "" : " (" + lieu.trim() + ")")
                        + " pour votre candidature " + c.getNumero() + ".");
        return e;
    }

    @Transactional
    public void reporter(Integer idEntretien, LocalDateTime nouvelleDate, String motif, Integer idAuteur) {
        EntretienModel e = charger(idEntretien);
        if (!e.isModifiable()) throw new RegleMetierException("Seul un entretien planifié peut être reporté.");
        if (nouvelleDate == null || nouvelleDate.isBefore(LocalDateTime.now())) {
            throw new RegleMetierException("La nouvelle date doit être dans le futur.");
        }
        if (motif == null || motif.isBlank()) throw new RegleMetierException("Le motif du report est obligatoire.");
        e.setDatePrevue(nouvelleDate);              // la date d'origine reste conservée
        e.setStatut(EntretienModel.REPORTE);
        e.setMotifReport(motif.trim());
        entretiens.save(e);
        Candidature c = candidature(e.getIdCandidature());
        historiser(c, "ENTRETIEN_" + e.getNiveau() + "_REPORTE", c.getStatut(), c.getStatut(), idAuteur,
                "Report au " + nouvelleDate.format(FR) + " (rendez-vous d'origine : " + e.getDateOrigine().format(FR)
                        + ") — " + motif.trim());
        notifierCandidat(c, "REPORT_ENTRETIEN_" + e.getNiveau() + "_" + nouvelleDate.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")),
                "Report de votre " + minuscule(e.getNiveauLibelle()),
                "Votre " + minuscule(e.getNiveauLibelle()) + " est reporté au " + nouvelleDate.format(FR) + ".");
    }

    @Transactional
    public void marquerAbsent(Integer idEntretien, Integer idAuteur) {
        EntretienModel e = charger(idEntretien);
        if (!e.isModifiable()) throw new RegleMetierException("Cet entretien n'est plus en attente.");
        e.setStatut(EntretienModel.ABSENT);
        e.setResultatPropose(EntretienModel.NON_ADMIS);
        e.setMotifProposition("Candidat absent à l'entretien.");
        entretiens.save(e);
        Candidature c = candidature(e.getIdCandidature());
        historiser(c, "ENTRETIEN_" + e.getNiveau() + "_ABSENT", c.getStatut(), c.getStatut(), idAuteur,
                "Absence enregistrée (rendez-vous du " + e.getDatePrevue().format(FR) + ").");
    }

    // ------------------------------------------------------------------ notes

    /** Saisie des notes (1 à 5) : calcule la note générale et propose un résultat. */
    @Transactional
    public EntretienModel noter(Integer idEntretien, Map<Integer, BigDecimal> notesParQuestion, Integer idAuteur) {
        EntretienModel e = charger(idEntretien);
        if (e.isDecide()) throw new RegleMetierException("Le résultat de cet entretien est déjà confirmé.");
        if (EntretienModel.ABSENT.equals(e.getStatut())) throw new RegleMetierException("Le candidat a été noté absent.");
        if (e.getDatePrevue().toLocalDate().isAfter(java.time.LocalDate.now())) {
            throw new RegleMetierException("Les notes se saisissent le jour de l'entretien ou après (prévu le "
                    + e.getDatePrevue().format(FR) + ").");
        }
        for (EntretienNoteModel n : e.getNotes()) {
            BigDecimal note = notesParQuestion == null ? null : notesParQuestion.get(n.getId());
            if (note == null) throw new RegleMetierException("Toutes les questions doivent être notées (« " + n.getQuestion() + " »).");
            if (note.compareTo(BigDecimal.ONE) < 0 || note.compareTo(BigDecimal.valueOf(5)) > 0) {
                throw new RegleMetierException("Les notes vont de 1 à 5 (« " + n.getQuestion() + " »).");
            }
            n.setNote(note.setScale(2, RoundingMode.HALF_UP));
        }
        calculer(e);
        e.setStatut(EntretienModel.REALISE);
        entretiens.save(e);
        Candidature c = candidature(e.getIdCandidature());
        historiser(c, "ENTRETIEN_" + e.getNiveau() + "_NOTE", c.getStatut(), c.getStatut(), idAuteur,
                "Note générale " + e.getNoteGenerale() + "/5 (seuil " + e.getSeuilGeneral() + ") — proposition : "
                        + e.getResultatPropose() + (e.getMotifProposition() == null ? "" : " — " + e.getMotifProposition()));
        return e;
    }

    /** Moyenne pondérée par l'importance, puis comparaison au seuil et aux questions éliminatoires. */
    static void calculer(EntretienModel e) {
        BigDecimal somme = BigDecimal.ZERO;
        BigDecimal poids = BigDecimal.ZERO;
        List<String> echecs = new ArrayList<>();
        for (EntretienNoteModel n : e.getNotes()) {
            BigDecimal imp = BigDecimal.valueOf(n.getImportance() == null ? 1 : n.getImportance());
            somme = somme.add(n.getNote().multiply(imp));
            poids = poids.add(imp);
            if (n.isEliminatoire() && n.getNoteMinimale() != null && n.getNote().compareTo(n.getNoteMinimale()) < 0) {
                echecs.add("« " + n.getQuestion() + " » " + n.getNote().stripTrailingZeros().toPlainString()
                        + " < " + n.getNoteMinimale().stripTrailingZeros().toPlainString());
            }
        }
        BigDecimal generale = somme.divide(poids, 2, RoundingMode.HALF_UP);
        e.setNoteGenerale(generale);
        boolean seuilAtteint = generale.compareTo(e.getSeuilGeneral()) >= 0;
        if (seuilAtteint && echecs.isEmpty()) {
            e.setResultatPropose(EntretienModel.ADMIS);
            e.setMotifProposition(null);
        } else {
            e.setResultatPropose(EntretienModel.NON_ADMIS);
            List<String> motifs = new ArrayList<>();
            if (!seuilAtteint) motifs.add("note générale inférieure au seuil " + e.getSeuilGeneral());
            if (!echecs.isEmpty()) motifs.add("question(s) éliminatoire(s) échouée(s) : " + String.join(", ", echecs));
            e.setMotifProposition(String.join(" ; ", motifs));
        }
    }

    // ------------------------------------------------------------------ décision RH

    @Transactional
    public CandidatureStatut decider(Integer idEntretien, boolean admis, String motif, Integer idAuteur) {
        EntretienModel e = charger(idEntretien);
        if (e.getResultatPropose() == null) throw new RegleMetierException("Saisissez d'abord les notes de l'entretien.");
        if (e.isDecide()) throw new RegleMetierException("Le résultat de cet entretien est déjà confirmé.");
        boolean suitProposition = admis == EntretienModel.ADMIS.equals(e.getResultatPropose());
        if (!suitProposition && (motif == null || motif.isBlank())) {
            throw new RegleMetierException("Un motif est obligatoire lorsque la décision diffère de la proposition ("
                    + e.getResultatPropose() + ").");
        }
        if (admis && EntretienModel.ABSENT.equals(e.getStatut())) {
            throw new RegleMetierException("Un candidat absent ne peut pas être admis : reportez l'entretien si l'absence est justifiée.");
        }
        Candidature c = candidature(e.getIdCandidature());
        if (niveauDuStatut(c.getStatut()) != e.getNiveau()) {
            throw new RegleMetierException("La candidature n'est plus à ce niveau d'entretien.");
        }
        e.setDecision(admis ? EntretienModel.ADMIS : EntretienModel.NON_ADMIS);
        e.setMotifDecision(motif == null || motif.isBlank() ? null : motif.trim());
        e.setIdDecideur(idAuteur);
        e.setDateDecision(LocalDateTime.now());
        entretiens.save(e);

        CandidatureStatut avant = c.getStatut();
        CandidatureStatut apres = admis ? statutSuivant(e.getNiveau()) : CandidatureStatut.NON_RETENUE;
        c.setStatut(apres);
        candidatures.save(c);
        historiser(c, "ENTRETIEN_" + e.getNiveau() + "_" + (admis ? "ADMIS" : "NON_ADMIS"), avant, apres, idAuteur,
                "Décision RH (" + e.getNiveauLibelle() + ") : " + (admis ? "admis" : "non admis")
                        + (e.getMotifDecision() == null ? "" : " — " + e.getMotifDecision()));

        if (!admis) {
            notifierCandidat(c, "ECHEC_ENTRETIEN_" + e.getNiveau(), "Réponse à votre candidature",
                    "Après votre " + minuscule(e.getNiveauLibelle()) + ", nous ne pouvons pas donner une suite favorable"
                            + " à votre candidature " + c.getNumero() + ". Votre dossier est conservé.");
        } else if (e.getNiveau() < 3) {
            notifierCandidat(c, "ADMIS_ENTRETIEN_" + e.getNiveau(), "Vous passez à l'étape suivante",
                    "Félicitations, vous avez réussi l'" + minuscule(e.getNiveauLibelle()) + ". Vous serez convoqué(e) à l'"
                            + minuscule(EntretienModel.libelleNiveau(e.getNiveau() + 1)) + ".");
        } else {
            notifierCandidat(c, "ADMIS_ENTRETIEN_3", "Vous avez réussi les entretiens",
                    "Vous avez réussi les trois entretiens. Votre dossier passe en confirmation finale.");
        }
        return apres;
    }

    // ------------------------------------------------------------------ outils

    /** « Entretien 1 — RH » -> « entretien 1 — RH » (seule la première lettre passe en minuscule). */
    private static String minuscule(String libelle) {
        return libelle == null || libelle.isEmpty() ? "" : Character.toLowerCase(libelle.charAt(0)) + libelle.substring(1);
    }

    private Candidature candidature(Integer id) {
        return candidatures.findWithCandidatAndOffreByIdCandidature(id)
                .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));
    }

    private void historiser(Candidature c, String action, CandidatureStatut avant, CandidatureStatut apres,
            Integer idAuteur, String commentaire) {
        HistoriqueCandidature h = new HistoriqueCandidature();
        h.setCandidature(c);
        h.setAction(action.length() > 40 ? action.substring(0, 40) : action);
        h.setAncienStatut(avant);
        h.setNouveauStatut(apres);
        h.setIdAuteur(idAuteur);
        h.setCommentaire(commentaire);
        historiques.save(h);
    }

    private void notifierCandidat(Candidature c, String type, String objet, String message) {
        try {
            notifications.notifierCandidat(c.getCandidat().getIdCandidat(), type.length() > 40 ? type.substring(0, 40) : type,
                    objet, message, c.getOffre() == null ? null : c.getOffre().getIdOffre(), c.getIdCandidature());
        } catch (RuntimeException ex) {
            log.warn("E-mail au candidat non créé pour {} ({}) : {}", c.getNumero(), type, ex.getMessage());
        }
    }
}
