package com.gestionrh.sirh.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.ModeleOnboardingService;
import com.gestionrh.sirh.service.dto.ModeleOnboardingForm;

@Controller
@RequestMapping("/rh/onboardings/modeles")
public class ModeleOnboardingController {

    private final ModeleOnboardingService service;
    private final EmployeRepository employes;
    private final PosteRepository postes;
    private final DepartementRepository departements;

    public ModeleOnboardingController(ModeleOnboardingService service, EmployeRepository employes,
                                      PosteRepository postes, DepartementRepository departements) {
        this.service = service;
        this.employes = employes;
        this.postes = postes;
        this.departements = departements;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("modeles", service.lister());
        return "rh/onboardings/modeles";
    }

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("form", service.formulaireNouveau());
        referentiels(model);
        return "rh/onboardings/modele-formulaire";
    }

    @PostMapping
    public String enregistrer(@ModelAttribute("form") ModeleOnboardingForm form,
                              BindingResult liaison, Authentication auth, Model model,
                              RedirectAttributes flash) {
        if (liaison.hasErrors()) {
            model.addAttribute("erreurs", java.util.List.of("Un champ du formulaire est invalide."));
            referentiels(model);
            return "rh/onboardings/modele-formulaire";
        }
        try {
            var modele = service.enregistrer(form, moi(auth));
            flash.addFlashAttribute("succes", "Modèle d'onboarding enregistré.");
            return "redirect:/rh/onboardings/modeles/" + modele.getId();
        } catch (Bloc4Exception e) {
            model.addAttribute("erreurs", java.util.List.of(e.getMessage()));
            referentiels(model);
            return "rh/onboardings/modele-formulaire";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("form", service.formulaireDe(id));
        referentiels(model);
        return "rh/onboardings/modele-formulaire";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Integer id, @ModelAttribute("form") ModeleOnboardingForm form,
                           BindingResult liaison, Authentication auth, Model model,
                           RedirectAttributes flash) {
        form.setId(id);
        if (liaison.hasErrors()) {
            model.addAttribute("erreurs", java.util.List.of("Un champ du formulaire est invalide."));
            referentiels(model);
            return "rh/onboardings/modele-formulaire";
        }
        try {
            var modele = service.enregistrer(form, moi(auth));
            flash.addFlashAttribute("succes", "Modèle d'onboarding enregistré.");
            return "redirect:/rh/onboardings/modeles/" + modele.getId();
        } catch (Bloc4Exception e) {
            model.addAttribute("erreurs", java.util.List.of(e.getMessage()));
            referentiels(model);
            return "rh/onboardings/modele-formulaire";
        }
    }

    @PostMapping("/{id}/activer")
    public String activer(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        service.activer(id, moi(auth));
        flash.addFlashAttribute("succes", "Version du modèle activée.");
        return "redirect:/rh/onboardings/modeles/" + id;
    }

    @PostMapping("/{id}/dupliquer")
    public String dupliquer(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        var copie = service.dupliquer(id, moi(auth));
        flash.addFlashAttribute("succes", "Modèle dupliqué.");
        return "redirect:/rh/onboardings/modeles/" + copie.getId();
    }

    @ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/onboardings/modeles";
    }

    private void referentiels(Model model) {
        model.addAttribute("postes", postes.findAllByOrderByDesignationAsc());
        model.addAttribute("departements", departements.findAllByOrderByDesignationAsc());
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc4Exception("Votre compte ne correspond à aucun employé."));
    }
}
