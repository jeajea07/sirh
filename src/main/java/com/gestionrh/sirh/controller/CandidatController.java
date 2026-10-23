package com.gestionrh.sirh.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gestionrh.sirh.model.CandidatModel;
import com.gestionrh.sirh.model.CandidatureModel;
import com.gestionrh.sirh.repository.Bloc2BCandidatRepository;
import com.gestionrh.sirh.repository.Bloc2BCandidatureRepository;

@Controller
public class CandidatController {

    private final Bloc2BCandidatRepository candidats;
    private final Bloc2BCandidatureRepository candidatures;

    public CandidatController(Bloc2BCandidatRepository candidats, Bloc2BCandidatureRepository candidatures) {
        this.candidats = candidats;
        this.candidatures = candidatures;
    }

    @GetMapping("/candidat")
    @Transactional(readOnly = true)
    public String index(Authentication auth, Model model) {
        CandidatModel candidat = trouverCandidatConnecte(auth);
        if (candidat == null) {
            return "redirect:/connexion?erreur";
        }

        List<CandidatureModel> mesCandidatures = candidatures.findAllWithCandidatAndTestByIdCandidat(candidat.getIdCandidat());

        model.addAttribute("candidat", candidat);
        model.addAttribute("candidatures", mesCandidatures);
        model.addAttribute("actif", "candidat");
        return "candidat/mes-candidatures";
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
}
