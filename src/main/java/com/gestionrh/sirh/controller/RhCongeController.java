package com.gestionrh.sirh.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.TypeCongeRepository;
import com.gestionrh.sirh.service.AbsenceService;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.CongeService;
import com.gestionrh.sirh.service.ManagerService;
import com.gestionrh.sirh.service.SoldeService;

/** Bloc 7 — espace RH : validation finale, absences, soldes et paramétrage des types. */
@Controller
@RequestMapping("/rh/conges")
public class RhCongeController extends EspaceTempsController {

    private final CongeService conges;
    private final AbsenceService absences;
    private final SoldeService soldes;
    private final TypeCongeRepository types;

    public RhCongeController(EmployeRepository employes, ManagerService managers, CongeService conges,
                             AbsenceService absences, SoldeService soldes, TypeCongeRepository types) {
        super(employes, managers);
        this.conges = conges;
        this.absences = absences;
        this.soldes = soldes;
        this.types = types;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("demandes", conges.aValiderParRh());
        model.addAttribute("enCours", conges.enCours());
        model.addAttribute("absences", absences.aDecider());
        List<EmployeModel> actifs = employes.findByActifTrueOrderByNomAsc();
        model.addAttribute("employes", actifs);
        Map<EmployeModel, List<SoldeService.Solde>> tableau = new LinkedHashMap<>();
        actifs.forEach(e -> tableau.put(e, soldes.soldes(e.getId())));
        model.addAttribute("soldes", tableau);
        model.addAttribute("typesAbsences", absences.typesDeclarables(true));
        model.addAttribute("types", types.findByActifTrueOrderByLibelleAsc());
        model.addAttribute("aujourdhui", LocalDate.now());
        return "rh/conges/index";
    }

    @PostMapping("/demandes/{id}/{decision}")
    public String decider(@PathVariable Integer id, @PathVariable String decision,
                          @RequestParam(required = false) String commentaire, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return switch (decision) {
            case "valider" -> action(flash, "Congé validé : planning et solde mis à jour.", "/rh/conges", () -> conges.validerRh(id, commentaire, moi));
            case "valider-manager" -> action(flash, "Étape manager validée (demande escaladée).", "/rh/conges", () -> conges.validerManager(id, commentaire, moi, true));
            case "refuser" -> action(flash, "Demande refusée.", "/rh/conges", () -> conges.refuser(id, commentaire, moi, true));
            default -> "redirect:/rh/conges";
        };
    }

    @PostMapping("/demandes/{id}/interrompre")
    public String interrompre(@PathVariable Integer id, @RequestParam(required = false) LocalDate dernierJour,
                              @RequestParam(required = false) String motif, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Congé interrompu : les jours non pris sont recrédités.", "/rh/conges",
                () -> conges.interrompre(id, dernierJour, motif, moi));
    }

    @PostMapping("/absences")
    public String saisirAbsence(@RequestParam(required = false) Integer idEmploye, @RequestParam(required = false) Integer idType,
                                @RequestParam(required = false) LocalDate dateDebut, @RequestParam(required = false) LocalDate dateFin,
                                @RequestParam(required = false) String commentaire,
                                @RequestParam(value = "justificatif", required = false) MultipartFile justificatif,
                                Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        if (idEmploye == null) {
            flash.addFlashAttribute("erreur", "Choisissez le salarié.");
            return "redirect:/rh/conges";
        }
        return action(flash, "Absence enregistrée.", "/rh/conges",
                () -> absences.declarer(idEmploye, idType, dateDebut, dateFin, commentaire, justificatif, moi, true));
    }

    @PostMapping("/absences/{id}/decider")
    public String deciderAbsence(@PathVariable Integer id, @RequestParam(required = false) String decision,
                                 @RequestParam(required = false) String commentaire,
                                 @RequestParam(defaultValue = "false") boolean accordSalarie,
                                 Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Décision enregistrée.", "/rh/conges",
                () -> absences.decider(id, decision, commentaire, accordSalarie, moi));
    }

    @PostMapping("/soldes/ajuster")
    public String ajuster(@RequestParam(required = false) Integer idEmploye, @RequestParam(required = false) String typeSolde,
                          @RequestParam(required = false) BigDecimal jours, @RequestParam(required = false) String motif,
                          Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        if (idEmploye == null) {
            flash.addFlashAttribute("erreur", "Choisissez le salarié.");
            return "redirect:/rh/conges";
        }
        return action(flash, "Solde ajusté.", "/rh/conges", () -> soldes.ajuster(idEmploye, typeSolde, jours, motif, moi));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/conges";
    }
}
