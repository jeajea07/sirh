package com.gestionrh.sirh.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.ReponseTestTechniqueModel;
import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.model.recrutement.Candidat;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.service.Bloc2BQuestion;
import com.gestionrh.sirh.service.Bloc2BService;

/** Test technique du Bloc 2B, greffe sur les candidatures du Bloc 2A. */
@Controller
public class TestTechniqueController {

    private final CandidatureRepository candidatures;
    private final CandidatRepository candidats;
    private final TestTechniqueRepository tests;
    private final ReponseTestTechniqueRepository reponses;
    private final Bloc2BService service;

    public TestTechniqueController(CandidatureRepository candidatures,
                                   CandidatRepository candidats,
                                   TestTechniqueRepository tests,
                                   ReponseTestTechniqueRepository reponses,
                                   Bloc2BService service) {
        this.candidatures = candidatures;
        this.candidats = candidats;
        this.tests = tests;
        this.reponses = reponses;
        this.service = service;
    }

    // ------------------------------------------------------------ espace candidat

    @GetMapping("/candidat/test/{idCandidature}")
    @Transactional(readOnly = true)
    public String testCandidat(@PathVariable Integer idCandidature, Authentication auth, Model model) {
        Candidature candidature = candidatureDuCandidat(idCandidature, auth, model);
        if (candidature == null) {
            model.addAttribute("questions", Bloc2BQuestion.QUESTIONS);
            model.addAttribute("noteSeuil", BigDecimal.ZERO);
            model.addAttribute("tempsRestant", "—");
            return "candidat/test-technique";
        }

        TestTechniqueModel test = tests.findByCandidature_IdCandidature(idCandidature).orElse(null);
        model.addAttribute("candidature", candidature);
        model.addAttribute("test", test);
        model.addAttribute("questions", Bloc2BQuestion.QUESTIONS);
        model.addAttribute("reponses", test == null ? List.of() : reponses.findByTestTechniqueOrderByIdReponseAsc(test));
        model.addAttribute("noteSeuil", test != null && test.getSeuilNote() != null ? test.getSeuilNote() : BigDecimal.ZERO);
        model.addAttribute("tempsRestant", test != null && test.getDateFin() != null ? "Test terminé" : "En cours");
        return "candidat/test-technique";
    }

    @PostMapping("/candidat/test/{idCandidature}")
    @Transactional
    public String soumettre(@PathVariable Integer idCandidature,
                            @RequestParam Map<String, String> formulaire,
                            Authentication auth, Model model, RedirectAttributes flash) {
        Candidature candidature = candidatureDuCandidat(idCandidature, auth, model);
        if (candidature == null) {
            flash.addFlashAttribute("erreur", "Candidature introuvable pour ce candidat.");
            return "redirect:/candidat";
        }
        Map<String, String> reponsesCandidat = new LinkedHashMap<>();
        for (Bloc2BQuestion question : Bloc2BQuestion.QUESTIONS) {
            reponsesCandidat.put(question.reference(), formulaire.get(question.reference()));
        }
        var resultat = service.soumettreTest(idCandidature, reponsesCandidat);
        flash.addFlashAttribute("succes", "Test enregistré. Note : " + resultat.note()
                + " / 3 (seuil " + resultat.seuil() + ").");
        return "redirect:/candidat";
    }

    // ------------------------------------------------------------ espace RH

    @GetMapping("/rh/tests")
    @PreAuthorize("hasRole('RH')")
    @Transactional(readOnly = true)
    public String testRh(Model model) {
        List<TestTechniqueModel> sessions = tests.findAllWithCandidatureAndCandidat();
        List<ResultatTest> resultats = new ArrayList<>();
        for (TestTechniqueModel test : sessions) {
            String candidat = "Candidat inconnu";
            Integer idCandidature = null;
            if (test.getCandidature() != null) {
                idCandidature = test.getCandidature().getIdCandidature();
                if (test.getCandidature().getCandidat() != null) {
                    candidat = test.getCandidature().getCandidat().getNom() + " "
                            + test.getCandidature().getCandidat().getPrenom();
                }
            }
            BigDecimal note = test.getNoteGenerale() == null ? BigDecimal.ZERO : test.getNoteGenerale();
            BigDecimal seuil = test.getSeuilNote() == null ? BigDecimal.ZERO : test.getSeuilNote();
            resultats.add(new ResultatTest(idCandidature, candidat, note.toPlainString(),
                    note.compareTo(seuil) >= 0, test.getStatut()));
        }

        model.addAttribute("noteSeuil", sessions.stream()
                .map(TestTechniqueModel::getSeuilNote)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(BigDecimal.ZERO));
        model.addAttribute("resultats", resultats);
        model.addAttribute("dossiersValides", candidatures.findByStatutOrderByDateValidationDesc(
                com.gestionrh.sirh.enums.recrutement.CandidatureStatut.DOSSIER_VALIDE));
        return "rh/test-technique";
    }

    @PostMapping("/rh/tests/{idCandidature}/acces")
    @PreAuthorize("hasRole('RH')")
    @Transactional
    public String creerAcces(@PathVariable Integer idCandidature,
                             @RequestParam(required = false) BigDecimal seuil,
                             RedirectAttributes flash) {
        var acces = service.creerAccesCandidat(idCandidature, seuil);
        flash.addFlashAttribute("succes", "Compte candidat créé : " + acces.identifiant()
                + " / mot de passe temporaire " + acces.motDePasseTemporaire());
        return "redirect:/rh/tests";
    }

    @PostMapping("/rh/tests/{idCandidature}/decision")
    @PreAuthorize("hasRole('RH')")
    @Transactional
    public String decider(@PathVariable Integer idCandidature,
                          @RequestParam String decision,
                          RedirectAttributes flash) {
        boolean admis = "admis".equalsIgnoreCase(decision);
        service.decider(idCandidature, admis);
        flash.addFlashAttribute("succes", admis
                ? "Candidature transmise à la sélection (Bloc 3)."
                : "Candidature écartée.");
        return "redirect:/rh/tests";
    }

    private Candidature candidatureDuCandidat(Integer idCandidature, Authentication auth, Model model) {
        Candidature candidature = candidatures.findById(idCandidature).orElse(null);
        if (candidature == null || candidature.getCandidat() == null || auth == null) {
            model.addAttribute("message", "Aucune candidature trouvée pour ce candidat.");
            return null;
        }
        Candidat connecte = candidats.findByEmailIgnoreCase(auth.getName())
                .or(() -> candidats.findByIdentifiant(auth.getName()))
                .orElse(null);
        if (connecte == null
                || !connecte.getIdCandidat().equals(candidature.getCandidat().getIdCandidat())) {
            model.addAttribute("message", "Cette candidature n'appartient pas à votre compte.");
            return null;
        }
        return candidature;
    }

    public record ResultatTest(Integer idCandidature, String candidat, String note, boolean admis, String statut) {
    }
}
