package com.gestionrh.sirh.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.service.recrutement.OffreService;

@Controller
public class AuthController {

    private final OffreService offres;

    public AuthController(OffreService offres) {
        this.offres = offres;
    }

    /** Page d'accueil publique : accès à l'espace de connexion ou aux offres d'emploi. */
    @GetMapping("/")
    public String racine(Authentication auth, Model model) {
        boolean connecte = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
        Page<Offre> dernieres = offres.offresOuvertes("", PageRequest.of(0, 3,
                Sort.by(Sort.Direction.DESC, "datePublication")));
        model.addAttribute("connecte", connecte);
        model.addAttribute("dernieresOffres", dernieres.getContent());
        model.addAttribute("nombreOffres", dernieres.getTotalElements());
        return "recrutement/vitrine/accueil";
    }

    // Le POST /connexion est géré par Spring Security, pas ici.
    @GetMapping("/connexion")
    public String connexion(Authentication auth) {
        boolean dejaConnecte = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
        return dejaConnecte ? "redirect:/accueil" : "connexion";
    }

    // login, role... sont ajoutés au modèle par LayoutAdvice
    @GetMapping("/accueil")
    public String accueil(Authentication auth) {
        boolean candidat = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CANDIDAT"));
        return candidat ? "redirect:/candidat" : "accueil";
    }

    @GetMapping("/acces-refuse")
    public String accesRefuse() {
        return "acces-refuse";
    }
}
