package com.gestionrh.sirh.controller;

import java.time.LocalDate;

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
import com.gestionrh.sirh.service.AbsenceService;
import com.gestionrh.sirh.service.CongeService;
import com.gestionrh.sirh.service.ManagerService;
import com.gestionrh.sirh.service.SoldeService;

/** Bloc 7 §15 — « Mes congés » : soldes, demandes, absences et justificatifs. */
@Controller
@RequestMapping("/conges")
public class CongeController extends EspaceTempsController {

    private final CongeService conges;
    private final AbsenceService absences;
    private final SoldeService soldes;

    public CongeController(EmployeRepository employes, ManagerService managers, CongeService conges,
                           AbsenceService absences, SoldeService soldes) {
        super(employes, managers);
        this.conges = conges;
        this.absences = absences;
        this.soldes = soldes;
    }

    @GetMapping
    public String page(Authentication auth, Model model) {
        EmployeModel moi = moi(auth);
        if (moi == null) {
            model.addAttribute("sansEmploye", true);
            return "conges";
        }
        model.addAttribute("soldes", soldes.soldes(moi.getId()));
        model.addAttribute("typesConges", conges.typesDemandables(false));
        model.addAttribute("typesAbsences", absences.typesDeclarables(false));
        model.addAttribute("demandes", conges.duSalarie(moi.getId()));
        model.addAttribute("absences", absences.duSalarie(moi.getId()));
        model.addAttribute("mouvements", soldes.historique(moi.getId()));
        model.addAttribute("manager", managers.managerDe(moi));
        model.addAttribute("aujourdhui", LocalDate.now());
        return "conges";
    }

    @PostMapping("/demandes")
    public String demander(@RequestParam(required = false) Integer idType,
                           @RequestParam(required = false) LocalDate dateDebut, @RequestParam(required = false) String demiJourneeDebut,
                           @RequestParam(required = false) LocalDate dateFin, @RequestParam(required = false) String demiJourneeFin,
                           @RequestParam(required = false) String motif,
                           @RequestParam(value = "justificatif", required = false) MultipartFile justificatif,
                           Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Demande envoyée.", "/conges", () -> conges.demander(moi.getId(), idType, dateDebut,
                demiJourneeDebut, dateFin, demiJourneeFin, motif, justificatif, moi, false));
    }

    @PostMapping("/demandes/{id}/annuler")
    public String annuler(@PathVariable Integer id, @RequestParam(required = false) String motif,
                          Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Demande annulée : les jours sont libérés.", "/conges", () -> conges.annuler(id, motif, moi));
    }

    @PostMapping("/demandes/{id}/justificatif")
    public String justificatifDemande(@PathVariable Integer id, @RequestParam("fichier") MultipartFile fichier,
                                      Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Justificatif ajouté.", "/conges", () -> conges.ajouterJustificatif(id, fichier, moi));
    }

    @PostMapping("/absences")
    public String declarer(@RequestParam(required = false) Integer idType,
                           @RequestParam(required = false) LocalDate dateDebut, @RequestParam(required = false) LocalDate dateFin,
                           @RequestParam(required = false) String commentaire,
                           @RequestParam(value = "justificatif", required = false) MultipartFile justificatif,
                           Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Absence déclarée.", "/conges",
                () -> absences.declarer(moi.getId(), idType, dateDebut, dateFin, commentaire, justificatif, moi, false));
    }

    @PostMapping("/absences/{id}/justificatif")
    public String justificatifAbsence(@PathVariable Integer id, @RequestParam("fichier") MultipartFile fichier,
                                      Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Justificatif déposé : il sera vérifié par le RH.", "/conges",
                () -> absences.deposerJustificatif(id, fichier, moi, false));
    }
}
