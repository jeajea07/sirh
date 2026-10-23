package com.gestionrh.sirh.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gestionrh.sirh.model.recrutement.Candidat;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;

/** Espace du candidat connecte : suivi de ses candidatures et acces au test technique. */
@Controller
public class CandidatController {

    private final CandidatRepository candidats;
    private final CandidatureRepository candidatures;

    public CandidatController(CandidatRepository candidats, CandidatureRepository candidatures) {
        this.candidats = candidats;
        this.candidatures = candidatures;
    }

    @GetMapping("/candidat")
    @Transactional(readOnly = true)
    public String index(Authentication auth, Model model) {
        Candidat candidat = trouverCandidatConnecte(auth);
        if (candidat == null) {
            return "redirect:/connexion?erreur";
        }

        List<Candidature> mesCandidatures =
                candidatures.findByCandidat_IdCandidatOrderByDateReceptionDesc(candidat.getIdCandidat());

        model.addAttribute("candidat", candidat);
        model.addAttribute("candidatures", mesCandidatures);
        model.addAttribute("actif", "candidat");
        return "candidat/mes-candidatures";
    }

    private Candidat trouverCandidatConnecte(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        String login = auth.getName().trim();
        return candidats.findByEmailIgnoreCase(login)
                .or(() -> candidats.findByIdentifiant(login))
                .orElse(null);
    }
}
