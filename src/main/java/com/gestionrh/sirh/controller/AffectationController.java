package com.gestionrh.sirh.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.service.AffectationService;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.dto.AffectationForm;

@Controller
@RequestMapping("/rh/affectations")
public class AffectationController {

    private final AffectationService service;
    private final EmployeRepository employes;
    private final PosteRepository postes;
    private final DepartementRepository departements;

    public AffectationController(AffectationService service, EmployeRepository employes,
                                 PosteRepository postes, DepartementRepository departements) {
        this.service = service;
        this.employes = employes;
        this.postes = postes;
        this.departements = departements;
    }

    @GetMapping
    public String liste(@RequestParam(required = false) Integer idEmploye, Model model) {
        model.addAttribute("affectations", idEmploye == null ? service.lister()
                : service.listerParEmploye(idEmploye));
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        model.addAttribute("idEmploye", idEmploye);
        return "rh/affectations/liste";
    }

    @GetMapping("/nouveau")
    public String nouveau(@RequestParam(required = false) Integer idEmploye, Model model) {
        model.addAttribute("form", service.formulaireNouvelle(idEmploye));
        referentiels(model);
        return "rh/affectations/formulaire";
    }

    @PostMapping
    public String enregistrer(@ModelAttribute("form") AffectationForm form, BindingResult liaison,
                              Authentication auth, Model model, RedirectAttributes flash) {
        try {
            service.enregistrer(form, moi(auth));
            flash.addFlashAttribute("succes", "Affectation enregistrée.");
            return "redirect:/rh/affectations";
        } catch (Bloc4Exception e) {
            model.addAttribute("erreurs", java.util.List.of(e.getMessage()));
            referentiels(model);
            return "rh/affectations/formulaire";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("affectation", service.detail(id));
        return "rh/affectations/detail";
    }

    @PostMapping("/{id}/cloturer")
    public String cloturer(@PathVariable Integer id,
                           @RequestParam("dateFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                           Authentication auth, RedirectAttributes flash) {
        service.cloturer(id, dateFin, moi(auth));
        flash.addFlashAttribute("succes", "Affectation clôturée.");
        return "redirect:/rh/affectations/" + id;
    }

    @ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/affectations";
    }

    private void referentiels(Model model) {
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        model.addAttribute("postes", postes.findAllByOrderByDesignationAsc());
        model.addAttribute("departements", departements.findAllByOrderByDesignationAsc());
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc4Exception("Votre compte ne correspond à aucun employé."));
    }
}
