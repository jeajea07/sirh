package com.gestionrh.sirh.controller;

import java.time.YearMonth;

import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.ManagerService;

/** Socle des écrans des Blocs 6 et 7 : salarié connecté, rôle, exécution d'une action avec message. */
abstract class EspaceTempsController {

    protected final EmployeRepository employes;
    protected final ManagerService managers;

    protected EspaceTempsController(EmployeRepository employes, ManagerService managers) {
        this.employes = employes;
        this.managers = managers;
    }

    /** Salarié connecté, ou null pour un compte sans employé (candidat). */
    protected EmployeModel moi(Authentication auth) {
        return auth == null ? null : employes.trouverParLogin(auth.getName()).orElse(null);
    }

    protected EmployeModel moiObligatoire(Authentication auth) {
        EmployeModel e = moi(auth);
        if (e == null) throw new Bloc4Exception("Votre compte ne correspond à aucun salarié.");
        return e;
    }

    protected boolean estRh(EmployeModel e) {
        return e != null && "RH".equals(managers.roleDe(e));
    }

    protected static YearMonth mois(String valeur) {
        try {
            return valeur == null || valeur.isBlank() ? YearMonth.now() : YearMonth.parse(valeur);
        } catch (RuntimeException e) {
            return YearMonth.now();
        }
    }

    /** Exécute une action métier : message de succès, ou erreur métier en message flash. */
    protected String action(RedirectAttributes flash, String succes, String redirection, Runnable traitement) {
        try {
            traitement.run();
            flash.addFlashAttribute("succes", succes);
        } catch (Bloc4Exception e) {
            flash.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:" + redirection;
    }
}
