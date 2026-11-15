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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.SalaireService;
import com.gestionrh.sirh.service.SalarieService;
import com.gestionrh.sirh.service.StatutSalarieService;
import com.gestionrh.sirh.service.dto.SalarieForm;

@Controller
@RequestMapping("/rh/salaries")
public class SalarieController {

    private final SalarieService service;
    private final EmployeRepository employes;
    private final DepartementRepository departements;
    private final SalaireService salaires;
    private final StatutSalarieService statuts;

    public SalarieController(SalarieService service, EmployeRepository employes,
                             DepartementRepository departements, SalaireService salaires,
                             StatutSalarieService statuts) {
        this.salaires = salaires;
        this.statuts = statuts;
        this.service = service;
        this.employes = employes;
        this.departements = departements;
    }

    @GetMapping
    public String liste(@RequestParam(required = false) String recherche,
                        @RequestParam(required = false) Integer idDepartement,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("salaries", service.lister(recherche, idDepartement, page, 12));
        model.addAttribute("departements", departements.findAllByOrderByDesignationAsc());
        model.addAttribute("recherche", recherche);
        model.addAttribute("idDepartement", idDepartement);
        return "rh/salaries/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        String role = model.getAttribute("role") == null ? "" : model.getAttribute("role").toString();
        model.addAttribute("detail", service.detail(id, role));
        model.addAttribute("motifsSortie", StatutSalarieService.MOTIFS_SORTIE);
        model.addAttribute("aujourdhui", java.time.LocalDate.now());
        return "rh/salaries/detail";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Integer id, Model model) {
        model.addAttribute("form", service.formulaireDe(id));
        model.addAttribute("salarie", service.detail(id, "RH").getSalarie());
        return "rh/salaries/formulaire";
    }

    @PostMapping("/{id}")
    public String enregistrer(@PathVariable Integer id, @ModelAttribute("form") SalarieForm form,
                              BindingResult liaison, Authentication auth, Model model,
                              RedirectAttributes flash) {
        form.setId(id);
        if (liaison.hasErrors()) {
            model.addAttribute("erreurs", java.util.List.of("Un champ du formulaire est invalide."));
            model.addAttribute("salarie", service.detail(id, "RH").getSalarie());
            return "rh/salaries/formulaire";
        }
        try {
            service.enregistrer(form, moi(auth));
            flash.addFlashAttribute("succes", "Informations du salarié mises à jour.");
            return "redirect:/rh/salaries/" + id;
        } catch (Bloc4Exception e) {
            model.addAttribute("erreurs", java.util.List.of(e.getMessage()));
            model.addAttribute("salarie", service.detail(id, "RH").getSalarie());
            return "rh/salaries/formulaire";
        }
    }

    // ------------------------------------------------------------ Bloc 5 : salaire et statut

    @PostMapping("/{id}/salaire")
    public String reviserSalaire(@PathVariable Integer id, @RequestParam(required = false) java.math.BigDecimal salaire,
                                 @RequestParam(required = false) java.time.LocalDate dateEffet,
                                 @RequestParam(required = false) String motif,
                                 @RequestParam(required = false) String commentaire,
                                 Authentication auth, RedirectAttributes flash) {
        return action(id, flash, "Salaire révisé.",
                () -> salaires.reviser(id, salaire, dateEffet, motif, commentaire, moi(auth)));
    }

    @PostMapping("/{id}/suspendre")
    public String suspendre(@PathVariable Integer id, @RequestParam(required = false) String motif,
                            @RequestParam(required = false) java.time.LocalDate dateEffet,
                            Authentication auth, RedirectAttributes flash) {
        return action(id, flash, "Contrat suspendu.", () -> statuts.suspendre(id, motif, dateEffet, moi(auth)));
    }

    @PostMapping("/{id}/reprendre")
    public String reprendre(@PathVariable Integer id, @RequestParam(required = false) java.time.LocalDate dateEffet,
                            Authentication auth, RedirectAttributes flash) {
        return action(id, flash, "Reprise enregistrée.", () -> statuts.reprendre(id, dateEffet, moi(auth)));
    }

    @PostMapping("/{id}/sortie")
    public String sortie(@PathVariable Integer id, @RequestParam(required = false) String motif,
                         @RequestParam(required = false) String commentaire,
                         @RequestParam(required = false) java.time.LocalDate dateNotification,
                         @RequestParam(required = false) java.time.LocalDate dateSortie,
                         Authentication auth, RedirectAttributes flash) {
        return action(id, flash, "Sortie enregistrée.",
                () -> statuts.enregistrerSortie(id, motif, commentaire, dateNotification, dateSortie, moi(auth)));
    }

    @PostMapping("/{id}/sortie/annuler")
    public String annulerSortie(@PathVariable Integer id, @RequestParam(required = false) String motif,
                                Authentication auth, RedirectAttributes flash) {
        return action(id, flash, "Sortie annulée.", () -> statuts.annulerSortie(id, motif, moi(auth)));
    }

    /** Exécute une action du dossier et revient sur la fiche, avec le message d'erreur métier le cas échéant. */
    private String action(Integer id, RedirectAttributes flash, String succes, Runnable traitement) {
        try {
            traitement.run();
            flash.addFlashAttribute("succes", succes);
        } catch (Bloc4Exception e) {
            flash.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/rh/salaries/" + id;
    }

    @ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/salaries";
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc4Exception("Votre compte ne correspond à aucun employé."));
    }
}
