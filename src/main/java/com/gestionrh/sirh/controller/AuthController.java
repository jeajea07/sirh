package com.gestionrh.sirh.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String racine() {
        return "redirect:/accueil";
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
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_CANDIDAT"))) {
            return "redirect:/candidat";
        }
        return "accueil";
    }

    @GetMapping("/acces-refuse")
    public String accesRefuse() {
        return "acces-refuse";
    }
}
