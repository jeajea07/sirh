package com.gestionrh.sirh.controller;

import java.util.Map;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

/**
 * Fournit à TOUTES les pages JSP les informations de l'utilisateur connecté
 * utilisées par le gabarit (barre latérale et barre du haut) :
 * login, role (ex. "RH"), roleCode (ex. "rh"), roleLibelle, initiales.
 * Les contrôleurs n'ont donc rien à ajouter au modèle pour le gabarit.
 */
@ControllerAdvice
public class LayoutAdvice {

    private static final Map<String, String> LIBELLES = Map.of(
            "EMPLOYE", "Employé",
            "RESPONSABLE", "Responsable de département",
            "RH", "Ressources humaines",
            "FINANCE", "Finance",
            "DG", "Direction générale");

    @ModelAttribute
    public void infosUtilisateur(Authentication auth, Model model) {
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
            return;
        }
        String role = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring("ROLE_".length()))
                .findFirst()
                .orElse("");

        model.addAttribute("login", auth.getName());
        model.addAttribute("role", role);
        model.addAttribute("roleCode", role.toLowerCase());
        model.addAttribute("roleLibelle", LIBELLES.getOrDefault(role, role));
        model.addAttribute("initiales", initiales(auth.getName()));
    }

    /** "jean.rakoto@x.mg" -> "JR" ; "EMP001" -> "EM". */
    private static String initiales(String login) {
        String base = login == null ? "" : login.trim();
        int arobase = base.indexOf('@');
        if (arobase > 0) {
            base = base.substring(0, arobase);
        }
        String[] mots = base.split("[._\\-\\s]+");
        if (mots.length >= 2 && !mots[0].isEmpty() && !mots[1].isEmpty()) {
            return ("" + mots[0].charAt(0) + mots[1].charAt(0)).toUpperCase();
        }
        return base.isEmpty() ? "?" : base.substring(0, Math.min(2, base.length())).toUpperCase();
    }
}
