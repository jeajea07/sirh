package com.gestionrh.sirh.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
import com.gestionrh.sirh.repository.TypeContratRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.ContratService;
import com.gestionrh.sirh.service.dto.ContratForm;
import com.gestionrh.sirh.service.dto.FiltreContrat;

@Controller
@RequestMapping("/rh/contrats")
public class ContratController {

    private final ContratService service;
    private final EmployeRepository employes;
    private final TypeContratRepository types;
    private final PosteRepository postes;
    private final DepartementRepository departements;

    public ContratController(ContratService service, EmployeRepository employes,
                             TypeContratRepository types, PosteRepository postes,
                             DepartementRepository departements) {
        this.service = service;
        this.employes = employes;
        this.types = types;
        this.postes = postes;
        this.departements = departements;
    }

    @GetMapping
    public String liste(@ModelAttribute("filtre") FiltreContrat filtre, Model model) {
        model.addAttribute("contrats", service.lister(filtre));
        return "rh/contrats/liste";
    }

    @GetMapping("/nouveau")
    public String nouveau(@RequestParam(required = false) Integer idEmploye, Model model) {
        model.addAttribute("form", service.formulaireNouveau(idEmploye));
        referentiels(model);
        return "rh/contrats/formulaire";
    }

    @PostMapping
    public String enregistrer(@ModelAttribute("form") ContratForm form,
                              BindingResult liaison,
                              @RequestParam(value = "action", defaultValue = "brouillon") String action,
                              Authentication auth, Model model, RedirectAttributes flash) {
        List<String> erreurs = new ArrayList<>();
        if (liaison.hasErrors()) {
            erreurs.add("Un champ du formulaire n'a pas un format valide.");
        }
        erreurs.addAll(service.verifier(form, form.getId() == null));
        if (!erreurs.isEmpty()) {
            model.addAttribute("erreurs", erreurs);
            referentiels(model);
            return "rh/contrats/formulaire";
        }
        var contrat = service.enregistrer(form, moi(auth));
        if ("soumettre".equals(action)) {
            service.soumettre(contrat.getId(), moi(auth));
            flash.addFlashAttribute("succes", "Contrat enregistré et soumis à validation.");
        } else {
            flash.addFlashAttribute("succes", "Contrat enregistré en brouillon.");
        }
        return "redirect:/rh/contrats/" + contrat.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("detail", service.detail(id));
        return "rh/contrats/detail";
    }

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Integer id, Model model) {
        model.addAttribute("form", service.formulaireDe(id));
        referentiels(model);
        return "rh/contrats/formulaire";
    }

    @PostMapping("/{id}/soumettre")
    public String soumettre(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        service.soumettre(id, moi(auth));
        flash.addFlashAttribute("succes", "Contrat soumis à validation.");
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Integer id,
                          @RequestParam(value = "commentaire", required = false) String commentaire,
                          Authentication auth, RedirectAttributes flash) {
        service.valider(id, commentaire, moi(auth));
        flash.addFlashAttribute("succes", "Contrat validé, en attente de signature.");
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/renvoyer")
    public String renvoyer(@PathVariable Integer id, @RequestParam("commentaire") String commentaire,
                           Authentication auth, RedirectAttributes flash) {
        service.renvoyer(id, commentaire, moi(auth));
        flash.addFlashAttribute("succes", "Contrat renvoyé au rédacteur.");
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/signer")
    public String signer(@PathVariable Integer id, @RequestParam("partie") String partie,
                         @RequestParam("nomSignataire") String nom,
                         @RequestParam("dateSignature") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                         Authentication auth, RedirectAttributes flash) {
        service.signer(id, partie, nom, date, moi(auth));
        flash.addFlashAttribute("succes", "Signature enregistrée.");
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/activer")
    public String activer(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        service.activer(id, moi(auth));
        flash.addFlashAttribute("succes", "Contrat activé.");
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/terminer")
    public String terminer(@PathVariable Integer id, @RequestParam("motif") String motif,
                           @RequestParam("dateFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
                           Authentication auth, RedirectAttributes flash) {
        service.terminer(id, motif, dateFin, moi(auth));
        flash.addFlashAttribute("succes", "Contrat terminé.");
        return "redirect:/rh/contrats/" + id;
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Integer id, @RequestParam("motif") String motif,
                          Authentication auth, RedirectAttributes flash) {
        service.annuler(id, motif, moi(auth));
        flash.addFlashAttribute("succes", "Contrat annulé.");
        return "redirect:/rh/contrats/" + id;
    }

    @GetMapping("/{id}/avenant")
    public String avenantForm(@PathVariable Integer id, Model model) {
        ContratForm f = service.formulaireCopie(id);
        f.setId(null);
        f.setTypeOperation("AVENANT");
        model.addAttribute("form", f);
        model.addAttribute("origine", service.detail(id).getContrat());
        referentiels(model);
        return "rh/contrats/avenant";
    }

    @PostMapping("/{id}/avenant")
    public String avenant(@PathVariable Integer id, @ModelAttribute("form") ContratForm form,
                          BindingResult liaison, Authentication auth, RedirectAttributes flash) {
        if (liaison.hasErrors()) {
            flash.addFlashAttribute("erreur", "Le formulaire contient un champ invalide.");
            return "redirect:/rh/contrats/" + id + "/avenant";
        }
        var contrat = service.creerAvenant(id, form, moi(auth));
        flash.addFlashAttribute("succes", "Avenant créé en brouillon.");
        return "redirect:/rh/contrats/" + contrat.getId();
    }

    @GetMapping("/{id}/renouveler")
    public String renouvelerForm(@PathVariable Integer id, Model model) {
        ContratForm f = service.formulaireCopie(id);
        f.setId(null);
        f.setTypeOperation("RENOUVELLEMENT");
        if (f.getDateFin() != null) {
            f.setDateDebut(f.getDateFin().plusDays(1));
            f.setDateFin(null);
        }
        model.addAttribute("form", f);
        model.addAttribute("origine", service.detail(id).getContrat());
        referentiels(model);
        return "rh/contrats/renouvellement";
    }

    @PostMapping("/{id}/renouveler")
    public String renouveler(@PathVariable Integer id, @ModelAttribute("form") ContratForm form,
                             BindingResult liaison, Authentication auth, RedirectAttributes flash) {
        if (liaison.hasErrors()) {
            flash.addFlashAttribute("erreur", "Le formulaire contient un champ invalide.");
            return "redirect:/rh/contrats/" + id + "/renouveler";
        }
        var contrat = service.renouveler(id, form, moi(auth));
        flash.addFlashAttribute("succes", "Renouvellement créé en brouillon.");
        return "redirect:/rh/contrats/" + contrat.getId();
    }

    @GetMapping("/echeances")
    public String echeances(@RequestParam(defaultValue = "30") int jours, Model model) {
        model.addAttribute("echeances", service.echeances(jours));
        model.addAttribute("jours", jours);
        return "rh/contrats/echeances";
    }

    @ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/contrats";
    }

    private void referentiels(Model model) {
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        model.addAttribute("typesContrat", types.findAllByOrderByIdAsc());
        model.addAttribute("postes", postes.findAllByOrderByDesignationAsc());
        model.addAttribute("departements", departements.findAllByOrderByDesignationAsc());
        model.addAttribute("aujourdhui", LocalDate.now());
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc4Exception("Votre compte ne correspond à aucun employé."));
    }
}
