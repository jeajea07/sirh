package com.gestionrh.sirh.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.DocumentRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.OrganismeRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.FormaliteService;
import com.gestionrh.sirh.service.dto.DeclarationForm;

@Controller
@RequestMapping("/rh/formalites")
public class FormaliteController {

    private final FormaliteService service;
    private final EmployeRepository employes;
    private final OrganismeRepository organismes;
    private final DocumentRepository documents;

    public FormaliteController(FormaliteService service, EmployeRepository employes,
                               OrganismeRepository organismes, DocumentRepository documents) {
        this.service = service;
        this.employes = employes;
        this.organismes = organismes;
        this.documents = documents;
    }

    @GetMapping
    public String liste(@RequestParam(required = false) String statut,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("formalites", service.lister(statut, page, 15));
        model.addAttribute("statut", statut);
        return "rh/formalites/liste";
    }

    @GetMapping("/nouvelle")
    public String nouvelle(@RequestParam(required = false) Integer idEmploye, Model model) {
        model.addAttribute("form", service.formulaireNouveau(idEmploye));
        referentiels(model);
        return "rh/formalites/formulaire";
    }

    @PostMapping
    public String enregistrer(@ModelAttribute("form") DeclarationForm form,
                              Authentication auth, Model model, RedirectAttributes flash) {
        try {
            var declaration = service.enregistrer(form, moi(auth));
            flash.addFlashAttribute("succes", "Déclaration enregistrée.");
            return "redirect:/rh/formalites/" + declaration.getId();
        } catch (Bloc4Exception e) {
            model.addAttribute("erreurs", java.util.List.of(e.getMessage()));
            model.addAttribute("form", form);
            referentiels(model);
            return "rh/formalites/formulaire";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        var formalite = service.detail(id);
        model.addAttribute("formalite", formalite);
        model.addAttribute("documents", documents.findByIdEmployeOrderByDateDepotDesc(formalite.getIdEmploye()));
        return "rh/formalites/detail";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Integer id, Model model) {
        model.addAttribute("form", service.formulaireDe(id));
        referentiels(model);
        return "rh/formalites/formulaire";
    }

    @PostMapping("/{id}")
    public String modifier(@PathVariable Integer id, @ModelAttribute("form") DeclarationForm form,
                           Authentication auth, Model model, RedirectAttributes flash) {
        form.setId(id);
        try {
            service.enregistrer(form, moi(auth));
            flash.addFlashAttribute("succes", "Déclaration mise à jour.");
            return "redirect:/rh/formalites/" + id;
        } catch (Bloc4Exception e) {
            model.addAttribute("erreurs", java.util.List.of(e.getMessage()));
            model.addAttribute("form", form);
            referentiels(model);
            return "rh/formalites/formulaire";
        }
    }

    @PostMapping("/{id}/preparer")
    public String preparer(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        service.marquerPreparee(id, moi(auth));
        flash.addFlashAttribute("succes", "Déclaration marquée préparée.");
        return "redirect:/rh/formalites/" + id;
    }

    @PostMapping("/{id}/transmettre")
    public String transmettre(@PathVariable Integer id,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateEnvoi,
                              @RequestParam String referenceExterne,
                              Authentication auth, RedirectAttributes flash) {
        service.marquerTransmise(id, dateEnvoi, referenceExterne, moi(auth));
        flash.addFlashAttribute("succes", "Déclaration transmise.");
        return "redirect:/rh/formalites/" + id;
    }

    @PostMapping("/{id}/confirmer")
    public String confirmer(@PathVariable Integer id, @RequestParam Integer idDocumentJustificatif,
                            Authentication auth, RedirectAttributes flash) {
        service.marquerConfirmee(id, idDocumentJustificatif, moi(auth));
        flash.addFlashAttribute("succes", "Déclaration confirmée.");
        return "redirect:/rh/formalites/" + id;
    }

    @PostMapping("/{id}/rejeter")
    public String rejeter(@PathVariable Integer id, @RequestParam String motif,
                          Authentication auth, RedirectAttributes flash) {
        service.rejeter(id, motif, moi(auth));
        flash.addFlashAttribute("succes", "Déclaration rejetée.");
        return "redirect:/rh/formalites/" + id;
    }

    @ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/formalites";
    }

    private void referentiels(Model model) {
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        model.addAttribute("organismes", organismes.findAllByOrderByNomAsc());
        model.addAttribute("documents", documents.findAll());
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc4Exception("Votre compte ne correspond à aucun employé."));
    }
}
