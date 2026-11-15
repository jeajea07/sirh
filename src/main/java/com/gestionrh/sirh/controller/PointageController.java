package com.gestionrh.sirh.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
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
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.JourneeTravailRepository;
import com.gestionrh.sirh.service.AnomalieService;
import com.gestionrh.sirh.service.ManagerService;
import com.gestionrh.sirh.service.PointageService;
import com.gestionrh.sirh.service.PrevisionService;

/** Bloc 6 — espace salarié : pointer, voir son planning, son relevé et ses anomalies (§15). */
@Controller
@RequestMapping("/pointage")
public class PointageController extends EspaceTempsController {

    private final PointageService pointages;
    private final AnomalieService anomalies;
    private final PrevisionService previsions;
    private final JourneeTravailRepository journees;

    public PointageController(EmployeRepository employes, ManagerService managers, PointageService pointages,
                              AnomalieService anomalies, PrevisionService previsions, JourneeTravailRepository journees) {
        super(employes, managers);
        this.pointages = pointages;
        this.anomalies = anomalies;
        this.previsions = previsions;
        this.journees = journees;
    }

    @GetMapping
    public String page(@RequestParam(required = false) String mois, Authentication auth, Model model) {
        EmployeModel moi = moi(auth);
        if (moi == null) {
            model.addAttribute("sansEmploye", true);
            return "pointage";
        }
        YearMonth m = mois(mois);
        LocalDate aujourdhui = LocalDate.now();
        model.addAttribute("pointagesDuJour", pointages.duJour(moi, aujourdhui));
        List<PrevisionService.Prevision> prochains = new ArrayList<>();
        List<LocalDate> jours = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            jours.add(aujourdhui.plusDays(i));
            prochains.add(previsions.previsionAffichee(moi, aujourdhui.plusDays(i)));
        }
        model.addAttribute("jours", jours);
        model.addAttribute("previsions", prochains);
        model.addAttribute("regime", previsions.regimeAu(moi.getId(), aujourdhui));
        model.addAttribute("journees", journees.findByEmploye_IdAndJourBetweenOrderByJourAsc(moi.getId(), m.atDay(1), m.atEndOfMonth()));
        model.addAttribute("anomalies", anomalies.duSalarie(moi));
        model.addAttribute("mois", m);
        return "pointage";
    }

    @PostMapping
    public String pointer(Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        try {
            var p = pointages.pointer(moi);
            flash.addFlashAttribute("succes", ("ENTREE".equals(p.getSens()) ? "Entrée" : "Sortie") + " enregistrée à " + p.getHeureFr() + ".");
        } catch (com.gestionrh.sirh.service.Bloc4Exception e) {
            flash.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/pointage";
    }

    @PostMapping("/anomalies/{id}/justifier")
    public String justifier(@PathVariable Integer id, @RequestParam(required = false) String justification,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime heure,
                            @RequestParam(required = false) String sens, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Justification transmise à votre manager.", "/pointage",
                () -> anomalies.justifier(id, moi, justification, heure, sens == null || sens.isBlank() ? null : sens));
    }
}
