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
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.ModeleOnboardingService;
import com.gestionrh.sirh.service.OnboardingService;
import com.gestionrh.sirh.service.dto.OnboardingForm;

@Controller
@RequestMapping("/rh/onboardings")
public class OnboardingController {

    private final OnboardingService service;
    private final ModeleOnboardingService modeles;
    private final EmployeRepository employes;
    private final ContratRepository contrats;

    public OnboardingController(OnboardingService service, ModeleOnboardingService modeles,
                                EmployeRepository employes, ContratRepository contrats) {
        this.service = service;
        this.modeles = modeles;
        this.employes = employes;
        this.contrats = contrats;
    }

    @GetMapping
    public String liste(@RequestParam(required = false) String statut,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("onboardings", service.lister(statut, page, 12));
        model.addAttribute("statut", statut);
        return "rh/onboardings/liste";
    }

    @GetMapping("/nouveau")
    public String nouveau(@RequestParam(required = false) Integer idEmploye,
                          @RequestParam(required = false) Integer idContrat, Model model) {
        OnboardingForm f = new OnboardingForm();
        f.setIdEmploye(idEmploye);
        f.setIdContrat(idContrat);
        f.setDateDebutPrevue(LocalDate.now());
        model.addAttribute("form", f);
        referentiels(model);
        return "rh/onboardings/formulaire";
    }

    @PostMapping
    public String creer(@ModelAttribute("form") OnboardingForm form, Authentication auth,
                        Model model, RedirectAttributes flash) {
        try {
            var onboarding = service.creer(form.getIdEmploye(), form.getIdContrat(),
                    form.getIdModeleOnboarding(), form.getDateDebutPrevue(), moi(auth));
            flash.addFlashAttribute("succes", "Onboarding créé.");
            return "redirect:/rh/onboardings/" + onboarding.getId();
        } catch (Bloc4Exception e) {
            model.addAttribute("erreurs", java.util.List.of(e.getMessage()));
            model.addAttribute("form", form);
            referentiels(model);
            return "rh/onboardings/formulaire";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        model.addAttribute("detail", service.detail(id));
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        return "rh/onboardings/detail";
    }

    @PostMapping("/{id}/taches/{idTache}/demarrer")
    public String demarrer(@PathVariable Integer id, @PathVariable Integer idTache,
                           Authentication auth, RedirectAttributes flash) {
        verifierAppartenance(id, idTache);
        service.demarrerTache(idTache, moi(auth));
        flash.addFlashAttribute("succes", "Tâche démarrée.");
        return "redirect:/rh/onboardings/" + id;
    }

    @PostMapping("/{id}/taches/{idTache}/terminer")
    public String terminer(@PathVariable Integer id, @PathVariable Integer idTache,
                           @RequestParam(required = false) String commentaire,
                           Authentication auth, RedirectAttributes flash) {
        verifierAppartenance(id, idTache);
        service.terminerTache(idTache, commentaire, moi(auth));
        flash.addFlashAttribute("succes", "Tâche terminée.");
        return "redirect:/rh/onboardings/" + id;
    }

    @PostMapping("/{id}/taches/{idTache}/bloquer")
    public String bloquer(@PathVariable Integer id, @PathVariable Integer idTache,
                          @RequestParam String motif, Authentication auth,
                          RedirectAttributes flash) {
        verifierAppartenance(id, idTache);
        service.bloquerTache(idTache, motif, moi(auth));
        flash.addFlashAttribute("succes", "Tâche bloquée.");
        return "redirect:/rh/onboardings/" + id;
    }

    @PostMapping("/{id}/taches/{idTache}/debloquer")
    public String debloquer(@PathVariable Integer id, @PathVariable Integer idTache,
                            Authentication auth, RedirectAttributes flash) {
        verifierAppartenance(id, idTache);
        service.debloquerTache(idTache, moi(auth));
        flash.addFlashAttribute("succes", "Tâche débloquée.");
        return "redirect:/rh/onboardings/" + id;
    }

    @PostMapping("/{id}/taches/{idTache}/reassigner")
    public String reassigner(@PathVariable Integer id, @PathVariable Integer idTache,
                             @RequestParam Integer idResponsable, Authentication auth,
                             RedirectAttributes flash) {
        verifierAppartenance(id, idTache);
        service.reassignerTache(idTache, idResponsable, moi(auth));
        flash.addFlashAttribute("succes", "Tâche réassignée.");
        return "redirect:/rh/onboardings/" + id;
    }

    @PostMapping("/{id}/cloturer")
    public String cloturer(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        service.cloturer(id, moi(auth));
        flash.addFlashAttribute("succes", "Onboarding clôturé.");
        return "redirect:/rh/onboardings/" + id;
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Integer id, @RequestParam String motif,
                          Authentication auth, RedirectAttributes flash) {
        service.annuler(id, motif, moi(auth));
        flash.addFlashAttribute("succes", "Onboarding annulé.");
        return "redirect:/rh/onboardings/" + id;
    }

    @PostMapping("/{id}/rouvrir")
    public String rouvrir(@PathVariable Integer id, @RequestParam String motif,
                          Authentication auth, RedirectAttributes flash) {
        service.rouvrir(id, motif, moi(auth));
        flash.addFlashAttribute("succes", "Onboarding rouvert.");
        return "redirect:/rh/onboardings/" + id;
    }

    @ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/onboardings";
    }

    private void verifierAppartenance(Integer idOnboarding, Integer idTache) {
        if (!service.tacheAppartient(idTache, idOnboarding)) {
            throw new Bloc4Exception("Cette tâche n'appartient pas à cet onboarding.");
        }
    }

    private void referentiels(Model model) {
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        model.addAttribute("contrats", contrats.findAll());
        model.addAttribute("modeles", modeles.lister().stream().filter(m -> m.isActif()).toList());
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc4Exception("Votre compte ne correspond à aucun employé."));
    }
}
