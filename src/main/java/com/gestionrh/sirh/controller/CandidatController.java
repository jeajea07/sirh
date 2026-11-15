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
    private final com.gestionrh.sirh.repository.EntretienRepository entretiens;

    public CandidatController(CandidatRepository candidats, CandidatureRepository candidatures,
                              com.gestionrh.sirh.repository.EntretienRepository entretiens) {
        this.candidats = candidats;
        this.candidatures = candidatures;
        this.entretiens = entretiens;
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
        // Entretien du niveau en cours (convocation visible par le candidat)
        java.util.Map<Integer, com.gestionrh.sirh.model.EntretienModel> convocations = new java.util.HashMap<>();
        if (!mesCandidatures.isEmpty()) {
            for (var e : entretiens.findByIdCandidatureIn(mesCandidatures.stream().map(Candidature::getIdCandidature).toList())) {
                var c = mesCandidatures.stream().filter(x -> x.getIdCandidature().equals(e.getIdCandidature())).findFirst().orElse(null);
                if (c != null && com.gestionrh.sirh.service.recrutement.EntretienService.niveauDuStatut(c.getStatut()) == e.getNiveau()
                        && e.isModifiable()) {
                    convocations.put(e.getIdCandidature(), e);
                }
            }
        }
        model.addAttribute("convocations", convocations);
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
