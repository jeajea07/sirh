package com.gestionrh.sirh.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.DemandeException;
import com.gestionrh.sirh.service.FinanceService;
import com.gestionrh.sirh.service.VerificationFinanceForm;

@Controller
@RequestMapping("/finance/demandes")
public class FinanceController {

    private final FinanceService service;
    private final EmployeRepository employes;

    public FinanceController(FinanceService service, EmployeRepository employes) {
        this.service = service;
        this.employes = employes;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ExceptionHandler(DemandeException.class)
    public String refus(DemandeException e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/finance/demandes";
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("demandes", service.aVerifier());
        return "finance/demandes";
    }

    @GetMapping("/historique")
    public String historique(Authentication auth, Model model) {
        model.addAttribute("lignes", service.historique(moi(auth).getId()));
        return "finance/historique";
    }

    @GetMapping("/{id}")
    public String verification(@PathVariable Integer id, Model model) {
        model.addAttribute("form", service.formulaireDe(id));
        model.addAttribute("verification", service.preparer(id, null));
        return "finance/verification";
    }

    @PostMapping("/{id}")
    public String agir(@PathVariable Integer id, @RequestParam("action") String action,
                       @ModelAttribute("form") VerificationFinanceForm form, BindingResult liaison,
                       Authentication auth, Model model, RedirectAttributes flash) {
        List<String> erreurs = new ArrayList<>();
        if (liaison.hasErrors()) {
            erreurs.add("Un nombre saisi n'a pas un format valide.");
        }
        erreurs.addAll(service.verifier(id, action, form));
        if (!erreurs.isEmpty()) {
            model.addAttribute("erreurs", erreurs);
            model.addAttribute("verification", service.preparer(id, form));
            return "finance/verification";
        }

        String message = service.decider(id, action, form, moi(auth));
        flash.addFlashAttribute("succes", message);
        if (FinanceService.ACTION_CORRIGER.equals(action)) {
            return "redirect:/finance/demandes/" + id;
        }
        return "redirect:/finance/demandes";
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Aucun employé ne correspond au compte " + auth.getName()));
    }
}
