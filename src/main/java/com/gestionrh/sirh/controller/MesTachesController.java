package com.gestionrh.sirh.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.OnboardingService;

@Controller
@RequestMapping("/mes-taches")
public class MesTachesController {

    private final OnboardingService service;
    private final EmployeRepository employes;

    public MesTachesController(OnboardingService service, EmployeRepository employes) {
        this.service = service;
        this.employes = employes;
    }

    @GetMapping
    public String liste(Authentication auth, Model model) {
        EmployeModel moi = moi(auth);
        model.addAttribute("taches", service.tachesDe(moi.getId()));
        model.addAttribute("retard", service.tachesEnRetard().stream()
                .filter(t -> t.getResponsableNom().equals(moi.getNomComplet())).toList());
        return "mes-taches";
    }

    @PostMapping
    public String action(@RequestParam String action, @RequestParam Integer idTache,
                         @RequestParam(required = false) String commentaire,
                         Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moi(auth);
        switch (action) {
            case "demarrer" -> service.demarrerTache(idTache, moi);
            case "terminer" -> service.terminerTache(idTache, commentaire, moi);
            case "bloquer" -> service.bloquerTache(idTache, commentaire, moi);
            case "debloquer" -> service.debloquerTache(idTache, moi);
            default -> throw new Bloc4Exception("Action inconnue.");
        }
        flash.addFlashAttribute("succes", "Tâche mise à jour.");
        return "redirect:/mes-taches";
    }

    @ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/mes-taches";
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc4Exception("Votre compte ne correspond à aucun employé."));
    }
}
