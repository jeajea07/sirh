package com.gestionrh.sirh.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.gestionrh.sirh.model.CandidatureModel;
import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.repository.CandidatureRepository;
import com.gestionrh.sirh.repository.ReponseTestTechniqueRepository;
import com.gestionrh.sirh.repository.TestTechniqueRepository;

@Controller
public class TestTechniqueController {

    private final CandidatureRepository candidatures;
    private final TestTechniqueRepository tests;
    private final ReponseTestTechniqueRepository reponses;

    public TestTechniqueController(CandidatureRepository candidatures,
                                  TestTechniqueRepository tests,
                                  ReponseTestTechniqueRepository reponses) {
        this.candidatures = candidatures;
        this.tests = tests;
        this.reponses = reponses;
    }

    @GetMapping("/candidat/test/{idCandidature}")
    public String testCandidat(@PathVariable Long idCandidature, Model model) {
        CandidatureModel candidature = candidatures.findById(idCandidature).orElse(null);
        if (candidature == null) {
            model.addAttribute("message", "Aucune candidature trouvée pour ce candidat.");
            model.addAttribute("reponses", List.of());
            model.addAttribute("test", null);
            model.addAttribute("noteSeuil", BigDecimal.ZERO);
            model.addAttribute("tempsRestant", "—");
            return "candidat/test-technique";
        }

        TestTechniqueModel test = tests.findByCandidature(candidature).orElse(null);
        model.addAttribute("candidature", candidature);
        model.addAttribute("test", test);
        model.addAttribute("reponses", test == null ? List.of() : reponses.findByTestTechniqueOrderByIdReponseAsc(test));
        model.addAttribute("noteSeuil", test != null && test.getSeuilNote() != null ? test.getSeuilNote() : BigDecimal.ZERO);
        model.addAttribute("tempsRestant", test != null && test.getDateFin() != null
                ? "Test terminé"
                : "En cours");
        return "candidat/test-technique";
    }

    @GetMapping("/rh/tests")
    public String testRh(Model model) {
        List<TestTechniqueModel> sessionTests = tests.findAll();
        List<ResultatTest> resultats = new ArrayList<>();

        for (TestTechniqueModel test : sessionTests) {
            String candidat = "Candidat inconnu";
            if (test.getCandidature() != null && test.getCandidature().getCandidat() != null) {
                candidat = test.getCandidature().getCandidat().getNom() + " " + test.getCandidature().getCandidat().getPrenom();
            }
            BigDecimal note = test.getNoteGenerale() == null ? BigDecimal.ZERO : test.getNoteGenerale();
            BigDecimal seuil = test.getSeuilNote() == null ? BigDecimal.ZERO : test.getSeuilNote();
            resultats.add(new ResultatTest(candidat, note.toPlainString(), note.compareTo(seuil) >= 0));
        }

        model.addAttribute("noteSeuil", sessionTests.stream()
                .map(TestTechniqueModel::getSeuilNote)
                .filter(java.util.Objects::nonNull)
                .findFirst()
                .orElse(BigDecimal.ZERO));
        model.addAttribute("resultats", resultats);
        return "rh/test-technique";
    }

    public record ResultatTest(String candidat, String note, boolean admis) {
    }
}
