package com.gestionrh.sirh.controller;

import java.time.LocalDate;
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

import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.repository.TypeContratRepository;
import com.gestionrh.sirh.service.DemandeException;
import com.gestionrh.sirh.service.DemandeForm;
import com.gestionrh.sirh.service.DemandeService;

@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeService service;
    private final EmployeRepository employes;
    private final PosteRepository postes;
    private final TypeContratRepository typesContrat;

    public DemandeController(DemandeService service, EmployeRepository employes,
                             PosteRepository postes, TypeContratRepository typesContrat) {
        this.service = service;
        this.employes = employes;
        this.postes = postes;
        this.typesContrat = typesContrat;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @ExceptionHandler(DemandeException.class)
    public String refus(DemandeException e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/demandes";
    }

    @GetMapping
    public String liste(Authentication auth, Model model) {
        model.addAttribute("demandes", service.lister(moi(auth)));
        return "demandes/liste";
    }

    @GetMapping("/nouvelle")
    public String nouvelle(Authentication auth, Model model) {
        EmployeModel moi = moi(auth);
        DemandeForm form = new DemandeForm();
        form.setDepartementId(moi.getDepartement().getId());
        preparerFormulaire(model, moi, form);
        return "demandes/formulaire";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Integer id, Authentication auth, Model model) {
        EmployeModel moi = moi(auth);
        preparerFormulaire(model, moi, service.formulaireDe(id, moi));
        return "demandes/formulaire";
    }

    @PostMapping
    public String enregistrer(@ModelAttribute("form") DemandeForm form, BindingResult liaison,
                              @RequestParam("action") String action,
                              Authentication auth, Model model, RedirectAttributes flash) {
        EmployeModel moi = moi(auth);
        boolean soumettre = "soumettre".equals(action);

        List<String> erreurs = new ArrayList<>();
        if (liaison.hasErrors()) {
            erreurs.add("Un nombre ou une date saisis n'ont pas un format valide");
        }
        erreurs.addAll(soumettre ? service.verifierSoumission(form) : service.verifierBrouillon(form));
        if (!erreurs.isEmpty()) {
            model.addAttribute("erreurs", erreurs);
            preparerFormulaire(model, moi, form);
            return "demandes/formulaire";
        }

        DemandeBesoinModel demande = service.enregistrer(form, moi);
        if (soumettre) {
            List<String> alertes = service.soumettre(demande.getId(), moi);
            flash.addFlashAttribute("succes", "Votre demande n° " + demande.getNumero() + " a été soumise.");
            if (!alertes.isEmpty()) {
                flash.addFlashAttribute("alertes", alertes);
            }
        } else {
            flash.addFlashAttribute("succes", "Brouillon enregistré. Vous pourrez le terminer plus tard.");
        }
        return "redirect:/demandes/" + demande.getId();
    }

    @GetMapping("/{id}")
    public String suivi(@PathVariable Integer id, Authentication auth, Model model) {
        model.addAttribute("suivi", service.suivre(id, moi(auth)));
        return "demandes/suivi";
    }

    @PostMapping("/{id}/soumettre")
    public String soumettre(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moi(auth);
        List<String> erreurs = service.verifierSoumission(service.formulaireDe(id, moi));
        if (!erreurs.isEmpty()) {
            flash.addFlashAttribute("erreurs", erreurs);
            return "redirect:/demandes/" + id + "/modifier";
        }
        List<String> alertes = service.soumettre(id, moi);
        flash.addFlashAttribute("succes", "Votre demande a été soumise.");
        if (!alertes.isEmpty()) {
            flash.addFlashAttribute("alertes", alertes);
        }
        return "redirect:/demandes/" + id;
    }

    @PostMapping("/{id}/retirer")
    public String retirer(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        service.retirer(id, moi(auth));
        flash.addFlashAttribute("succes", "La demande a été retirée.");
        return "redirect:/demandes/" + id;
    }

    private EmployeModel moi(Authentication auth) {
        EmployeModel e = employes.trouverParLogin(auth.getName()).orElse(null);
        if (e == null) {
            throw new IllegalStateException("Aucun employé ne correspond au compte " + auth.getName());
        }
        return e;
    }

    private void preparerFormulaire(Model model, EmployeModel moi, DemandeForm form) {
        model.addAttribute("form", form);
        model.addAttribute("demandeur", moi);
        model.addAttribute("postes", postes.findAllByOrderByDesignationAsc());
        model.addAttribute("typesContrat", typesContrat.findAllByOrderByIdAsc());
        model.addAttribute("aujourdhui", LocalDate.now().toString());
    }
}
