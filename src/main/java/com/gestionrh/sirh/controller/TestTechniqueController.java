package com.gestionrh.sirh.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.CandidatModel;
import com.gestionrh.sirh.model.CandidatureModel;
import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.repository.CandidatRepository;
import com.gestionrh.sirh.repository.CandidatureRepository;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;
import com.gestionrh.sirh.service.Bloc2BService;
import com.gestionrh.sirh.service.Bloc2BStatus;

@Controller
public class TestTechniqueController {

    private final CandidatureRepository candidatures;
    private final TestTechniqueRepository tests;
    private final ReponseTestTechniqueRepository reponses;
    private final CandidatRepository candidats;
    private final Bloc2BService bloc2BService;

    public TestTechniqueController(CandidatureRepository candidatures,
                                  TestTechniqueRepository tests,
                                  ReponseTestTechniqueRepository reponses,
                                  CandidatRepository candidats,
                                  Bloc2BService bloc2BService) {
        this.candidatures = candidatures;
        this.tests = tests;
        this.reponses = reponses;
        this.candidats = candidats;
        this.bloc2BService = bloc2BService;
    }

    @GetMapping("/candidat/test/{idCandidature}")
    public String testCandidat(@PathVariable Long idCandidature, Authentication auth, Model model) {
        CandidatureModel candidature = candidatures.findById(idCandidature).orElse(null);
        if (candidature == null || candidature.getCandidat() == null) {
            model.addAttribute("message", "Aucune candidature trouvée pour ce candidat.");
            model.addAttribute("reponses", List.of());
            model.addAttribute("test", null);
            model.addAttribute("noteSeuil", BigDecimal.ZERO);
            model.addAttribute("tempsRestant", "—");
            return "candidat/test-technique";
        }

        CandidatModel candidatConnecte = trouverCandidatConnecte(auth);
        if (candidatConnecte == null || !candidatConnecte.getIdCandidat().equals(candidature.getCandidat().getIdCandidat())) {
            return "redirect:/acces-refuse";
        }

        TestTechniqueModel test = tests.findByCandidature(candidature).orElse(null);
        model.addAttribute("candidature", candidature);
        model.addAttribute("test", test);
        model.addAttribute("reponses", test == null ? List.of() : reponses.findByTestTechniqueOrderByIdReponseAsc(test));
        model.addAttribute("noteSeuil", test != null && test.getSeuilNote() != null ? test.getSeuilNote() : Bloc2BService.SEUIL_PAR_DEFAUT);
        model.addAttribute("tempsRestant", test != null && test.getDateFin() != null
                ? "Test terminé"
                : "En cours");
        return "candidat/test-technique";
    }

    @PostMapping("/candidat/test/{idCandidature}")
    public String soumettreTest(@PathVariable Long idCandidature,
                                Authentication auth,
                                @RequestParam Map<String, String> parametres,
                                RedirectAttributes flash) {
        CandidatureModel candidature = candidatures.findById(idCandidature).orElse(null);
        CandidatModel candidatConnecte = trouverCandidatConnecte(auth);
        if (candidature == null || candidature.getCandidat() == null || candidatConnecte == null
                || !candidatConnecte.getIdCandidat().equals(candidature.getCandidat().getIdCandidat())) {
            return "redirect:/acces-refuse";
        }

        bloc2BService.soumettreTestTechnique(idCandidature, parametres);
        flash.addFlashAttribute("succes", "Votre test technique a bien été soumis.");
        return "redirect:/candidat/test/" + idCandidature;
    }

    @GetMapping("/rh/tests")
    @Transactional(readOnly = true)
    public String testRh(Model model) {
        List<TestTechniqueModel> sessionTests = tests.findAllWithCandidatureAndCandidat();
        List<ResultatTest> resultats = new ArrayList<>();

        for (TestTechniqueModel test : sessionTests) {
            String candidat = "Candidat inconnu";
            if (test.getCandidature() != null && test.getCandidature().getCandidat() != null) {
                candidat = test.getCandidature().getCandidat().getNom() + " " + test.getCandidature().getCandidat().getPrenom();
            }
            BigDecimal note = test.getNoteGenerale() == null ? BigDecimal.ZERO : test.getNoteGenerale();
            BigDecimal seuil = test.getSeuilNote() == null ? BigDecimal.ZERO : test.getSeuilNote();
            resultats.add(new ResultatTest(candidat, note.toPlainString(), note.compareTo(seuil) >= 0, test.getCandidature() != null ? test.getCandidature().getIdCandidature() : null));
        }

        model.addAttribute("noteSeuil", sessionTests.stream()
                .map(TestTechniqueModel::getSeuilNote)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(Bloc2BService.SEUIL_PAR_DEFAUT));
        model.addAttribute("resultats", resultats);
        return "rh/test-technique";
    }

    @PostMapping("/rh/tests/{idCandidature}/decision")
    public String decisionRh(@PathVariable Long idCandidature,
                            @RequestParam String decision,
                            RedirectAttributes flash) {
        if ("admis".equals(decision)) {
            Bloc2BService.Bloc2BResultat resultat = bloc2BService.confirmerCandidatureAdmise(idCandidature, Bloc2BService.SEUIL_PAR_DEFAUT);
            flash.addFlashAttribute("succes", resultat.admis() ? "La candidature a été admise." : "La candidature n’a pas atteint le seuil.");
        } else {
            CandidatureModel candidature = candidatures.findById(idCandidature).orElse(null);
            if (candidature != null) {
                candidature.setStatut(Bloc2BStatus.CANDIDATURE_REFUSE);
                candidature.setNoteFinale(0D);
                candidatures.save(candidature);
            }
            flash.addFlashAttribute("succes", "La candidature a été refusée.");
        }
        return "redirect:/rh/tests";
    }

    private CandidatModel trouverCandidatConnecte(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        String login = auth.getName().trim();
        return candidats.findByEmail(login)
                .or(() -> candidats.findByIdentifiant(login))
                .orElse(null);
    }

    public record ResultatTest(String candidat, String note, boolean admis, Long idCandidature) {
    }
}
