package com.gestionrh.sirh.controller;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.PlanningModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.ManagerService;
import com.gestionrh.sirh.service.PlanningService;
import com.gestionrh.sirh.service.ReferentielTempsService;

/** Bloc 6 §6 — plannings : préparation (manager), validation RH → Finance → DG. */
@Controller
@RequestMapping("/planning")
public class PlanningController extends EspaceTempsController {

    private final PlanningService plannings;
    private final ReferentielTempsService referentiel;

    public PlanningController(EmployeRepository employes, ManagerService managers, PlanningService plannings,
                              ReferentielTempsService referentiel) {
        super(employes, managers);
        this.plannings = plannings;
        this.referentiel = referentiel;
    }

    @GetMapping
    public String liste(Authentication auth, Model model) {
        EmployeModel moi = moiObligatoire(auth);
        model.addAttribute("plannings", plannings.visiblesPar(moi));
        model.addAttribute("aValider", plannings.aValiderPar(moi));
        model.addAttribute("departements", plannings.departementsPreparables(moi));
        LocalDate lundi = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);
        model.addAttribute("debutPropose", lundi);
        model.addAttribute("finPropose", lundi.plusDays(6));
        return "planning/liste";
    }

    @PostMapping
    public String creer(@RequestParam(required = false) Integer idDepartement,
                        @RequestParam(required = false) LocalDate debut, @RequestParam(required = false) LocalDate fin,
                        Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        try {
            PlanningModel p = plannings.creer(idDepartement, debut, fin, moi);
            flash.addFlashAttribute("succes", "Planning créé et pré-rempli (régime, fériés, congés validés). Ajustez puis soumettez.");
            return "redirect:/planning/" + p.getId();
        } catch (Bloc4Exception e) {
            flash.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/planning";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Authentication auth, Model model) {
        EmployeModel moi = moiObligatoire(auth);
        PlanningModel p = plannings.charger(id);
        String role = managers.roleDe(moi);
        boolean preparateur = plannings.peutPreparer(moi, p.getDepartement());
        if (!preparateur && !Set.of("RH", "FINANCE", "DG").contains(role)) {
            throw new Bloc4Exception("Ce planning ne vous est pas accessible.");
        }
        model.addAttribute("planning", p);
        model.addAttribute("rangees", plannings.grille(id));
        model.addAttribute("jours", plannings.jours(p));
        model.addAttribute("modeles", referentiel.modelesActifs());
        model.addAttribute("editable", preparateur && p.isModifiable());
        model.addAttribute("preparateur", preparateur);
        model.addAttribute("budgetRestant", com.gestionrh.sirh.util.Formats.montant(plannings.budgetRestant(p)));
        model.addAttribute("controles", p.isModifiable() ? plannings.controler(p) : java.util.List.of());
        boolean peutValider = ("SOUMIS_RH".equals(p.getStatut()) && "RH".equals(role))
                || ("SOUMIS_FINANCE".equals(p.getStatut()) && "FINANCE".equals(role))
                || ("ARBITRAGE_DG".equals(p.getStatut()) && "DG".equals(role));
        model.addAttribute("peutValider", peutValider);
        return "planning/detail";
    }

    @PostMapping("/{id}/grille")
    public String grille(@PathVariable Integer id, @RequestParam Map<String, String> valeurs,
                         @RequestParam(required = false) String soumettre, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        try {
            plannings.enregistrerGrille(id, valeurs, moi);
            if (soumettre != null) {
                plannings.soumettre(id, moi);
                flash.addFlashAttribute("succes", "Planning soumis au RH.");
            } else {
                flash.addFlashAttribute("succes", "Planning enregistré.");
            }
        } catch (Bloc4Exception e) {
            flash.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/planning/" + id;
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Validation enregistrée.", "/planning/" + id, () -> plannings.valider(id, moi));
    }

    @PostMapping("/{id}/rejeter")
    public String rejeter(@PathVariable Integer id, @RequestParam(required = false) String motif,
                          Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Planning rejeté et renvoyé au manager.", "/planning/" + id, () -> plannings.rejeter(id, motif, moi));
    }

    @PostMapping("/{id}/nouvelle-version")
    public String nouvelleVersion(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        try {
            PlanningModel p = plannings.nouvelleVersion(id, moi);
            flash.addFlashAttribute("succes", "Version " + p.getVersion() + " créée : elle repassera le circuit de validation.");
            return "redirect:/planning/" + p.getId();
        } catch (Bloc4Exception e) {
            flash.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/planning/" + id;
        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/planning";
    }
}
