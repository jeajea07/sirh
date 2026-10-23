package com.gestionrh.sirh.controller;

import java.util.List;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.DemandeException;
import com.gestionrh.sirh.service.DgService;

@Controller
@RequestMapping("/dg/demandes")
public class DgController {

    private final DgService service;
    private final EmployeRepository employes;

    public DgController(DgService service, EmployeRepository employes) {
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
        return "redirect:/dg/demandes";
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("demandes", service.aValider());
        return "dg/demandes";
    }

    @GetMapping("/historique")
    public String historique(Authentication auth, Model model) {
        model.addAttribute("lignes", service.historique(moi(auth).getId()));
        return "dg/historique";
    }

    @GetMapping("/{id}")
    public String verification(@PathVariable Integer id, Model model) {
        model.addAttribute("demande", service.demandeEnAttente(id));
        model.addAttribute("dateReception", service.dateReception(id));
        model.addAttribute("commentaire", "");
        return "dg/verification";
    }

    @PostMapping("/{id}")
    public String agir(@PathVariable Integer id, @RequestParam("action") String action,
                       @RequestParam(value = "commentaire", required = false) String commentaire,
                       Authentication auth, Model model, RedirectAttributes flash) {
        List<String> erreurs = service.verifier(id, action, commentaire);
        if (!erreurs.isEmpty()) {
            model.addAttribute("demande", service.demandeEnAttente(id));
            model.addAttribute("dateReception", service.dateReception(id));
            model.addAttribute("commentaire", commentaire);
            model.addAttribute("erreurs", erreurs);
            return "dg/verification";
        }
        flash.addFlashAttribute("succes", service.decider(id, action, commentaire, moi(auth)));
        return "redirect:/dg/demandes";
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new IllegalStateException("Aucun employé ne correspond au compte " + auth.getName()));
    }
}
