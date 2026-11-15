package com.gestionrh.sirh.controller;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.gestionrh.sirh.repository.PointageRepository;
import com.gestionrh.sirh.service.AnomalieService;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.CalculTempsService;
import com.gestionrh.sirh.service.CloturePaieService;
import com.gestionrh.sirh.service.ManagerService;
import com.gestionrh.sirh.service.PlanningService;
import com.gestionrh.sirh.service.PointageService;
import com.gestionrh.sirh.service.ReferentielTempsService;

/** Bloc 6 — espace RH : anomalies, relevés et pointages manuels, clôture de période, paramétrage. */
@Controller
@RequestMapping("/rh/temps")
public class RhTempsController extends EspaceTempsController {

    private final AnomalieService anomalies;
    private final PlanningService plannings;
    private final PointageService pointages;
    private final CalculTempsService calcul;
    private final CloturePaieService cloture;
    private final ReferentielTempsService referentiel;
    private final JourneeTravailRepository journees;
    private final PointageRepository pointagesRepo;

    public RhTempsController(EmployeRepository employes, ManagerService managers, AnomalieService anomalies,
                             PlanningService plannings, PointageService pointages, CalculTempsService calcul,
                             CloturePaieService cloture, ReferentielTempsService referentiel,
                             JourneeTravailRepository journees, PointageRepository pointagesRepo) {
        super(employes, managers);
        this.anomalies = anomalies;
        this.plannings = plannings;
        this.pointages = pointages;
        this.calcul = calcul;
        this.cloture = cloture;
        this.referentiel = referentiel;
        this.journees = journees;
        this.pointagesRepo = pointagesRepo;
    }

    @GetMapping
    public String index(Authentication auth, Model model) {
        EmployeModel moi = moiObligatoire(auth);
        model.addAttribute("anomalies", anomalies.aTraiterPar(moi, true));
        model.addAttribute("plannings", plannings.aValiderPar(moi));
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        model.addAttribute("moisPrecedent", YearMonth.now().minusMonths(1));
        model.addAttribute("miseEnService", com.gestionrh.sirh.util.Formats.date(calcul.dateMiseEnService()));
        return "rh/temps/index";
    }

    @PostMapping("/anomalies/{id}/{decision}")
    public String decider(@PathVariable Integer id, @PathVariable String decision,
                          @RequestParam(required = false) String commentaire, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return switch (decision) {
            case "accepter" -> action(flash, "Régularisation acceptée.", "/rh/temps", () -> anomalies.accepter(id, commentaire, moi, true));
            case "refuser" -> action(flash, "Anomalie confirmée.", "/rh/temps", () -> anomalies.refuser(id, commentaire, moi, true));
            case "classer" -> action(flash, "Anomalie classée sans suite.", "/rh/temps", () -> anomalies.classer(id, commentaire, moi));
            default -> "redirect:/rh/temps";
        };
    }

    // ------------------------------------------------------------ relevé d'un salarié

    @GetMapping("/releve")
    public String releve(@RequestParam(required = false) Integer idEmploye, @RequestParam(required = false) String mois, Model model) {
        YearMonth m = mois(mois);
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        model.addAttribute("mois", m);
        if (idEmploye != null) {
            EmployeModel e = employes.findById(idEmploye).orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
            model.addAttribute("salarie", e);
            model.addAttribute("journees", journees.findByEmploye_IdAndJourBetweenOrderByJourAsc(idEmploye, m.atDay(1), m.atEndOfMonth()));
            model.addAttribute("pointages", pointagesRepo.valides(idEmploye, m.atDay(1).atStartOfDay(), m.atEndOfMonth().plusDays(1).atStartOfDay()));
            model.addAttribute("regime", referentiel.regimeEnCours(idEmploye));
            model.addAttribute("regimes", referentiel.regimes());
        }
        return "rh/temps/releve";
    }

    @PostMapping("/pointages")
    public String pointageManuel(@RequestParam Integer idEmploye,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime horodatage,
                                 @RequestParam(required = false) String sens, @RequestParam(required = false) String motif,
                                 Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        EmployeModel e = employes.findById(idEmploye).orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        String retour = "/rh/temps/releve?idEmploye=" + idEmploye + (horodatage == null ? "" : "&mois=" + YearMonth.from(horodatage));
        return action(flash, "Pointage manuel enregistré, journée recalculée.", retour,
                () -> pointages.saisirManuellement(e, horodatage, sens, motif, null, moi));
    }

    @PostMapping("/pointages/{id}/annuler")
    public String annulerPointage(@PathVariable Integer id, @RequestParam Integer idEmploye, @RequestParam(required = false) String motif,
                                  @RequestParam(required = false) String mois, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Pointage annulé, journée recalculée.", "/rh/temps/releve?idEmploye=" + idEmploye + "&mois=" + mois(mois),
                () -> pointages.annuler(id, motif, moi));
    }

    @PostMapping("/recalculer")
    public String recalculer(@RequestParam Integer idEmploye, @RequestParam(required = false) String mois, RedirectAttributes flash) {
        YearMonth m = mois(mois);
        EmployeModel e = employes.findById(idEmploye).orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        return action(flash, "Relevé recalculé.", "/rh/temps/releve?idEmploye=" + idEmploye + "&mois=" + m,
                () -> calcul.recalculer(e, m.atDay(1), m.atEndOfMonth()));
    }

    @PostMapping("/regime")
    public String changerRegime(@RequestParam Integer idEmploye, @RequestParam(required = false) Integer idRegime,
                                @RequestParam(required = false) LocalDate dateEffet, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Régime de travail modifié.", "/rh/temps/releve?idEmploye=" + idEmploye,
                () -> referentiel.changerRegime(idEmploye, idRegime, dateEffet, moi));
    }

    // ------------------------------------------------------------ clôture et paie

    @GetMapping("/periodes")
    public String periodes(@RequestParam(required = false) String mois, @RequestParam(required = false) Integer voir, Model model) {
        YearMonth m = mois == null ? YearMonth.now().minusMonths(1) : mois(mois);
        model.addAttribute("mois", m);
        model.addAttribute("blocages", cloture.blocages(m.getYear(), m.getMonthValue()));
        model.addAttribute("periodes", cloture.periodes());
        if (voir != null) {
            model.addAttribute("voir", voir);
            model.addAttribute("variables", cloture.variables(voir));
        }
        return "rh/temps/periodes";
    }

    @PostMapping("/periodes/cloturer")
    public String cloturer(@RequestParam String mois, Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        YearMonth m = mois(mois);
        try {
            var p = cloture.cloturer(m.getYear(), m.getMonthValue(), moi);
            flash.addFlashAttribute("succes", "Période " + p.getLibelle() + " clôturée : variables de paie générées, congés acquis.");
            return "redirect:/rh/temps/periodes?mois=" + m + "&voir=" + p.getId();
        } catch (Bloc4Exception e) {
            flash.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/rh/temps/periodes?mois=" + m;
        }
    }

    @GetMapping("/periodes/{id}/export.csv")
    public ResponseEntity<byte[]> exporter(@PathVariable Integer id) {
        byte[] csv = ("﻿" + cloture.exporter(id)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"variables-paie-" + id + ".csv\"")
                .body(csv);
    }

    // ------------------------------------------------------------ paramétrage

    @GetMapping("/parametres")
    public String parametres(Model model) {
        model.addAttribute("modeles", referentiel.modeles());
        model.addAttribute("regimes", referentiel.regimes());
        model.addAttribute("feries", referentiel.feries());
        return "rh/temps/parametres";
    }

    @PostMapping("/parametres/modeles")
    public String creerModele(@RequestParam(required = false) String code, @RequestParam(required = false) String libelle,
                              @RequestParam(required = false) LocalTime heureDebut, @RequestParam(required = false) LocalTime heureFin,
                              @RequestParam(defaultValue = "0") int pause, @RequestParam(defaultValue = "10") int toleranceRetard,
                              @RequestParam(defaultValue = "10") int toleranceDepart, @RequestParam(defaultValue = "false") boolean nuit,
                              RedirectAttributes flash) {
        return action(flash, "Modèle horaire créé.", "/rh/temps/parametres",
                () -> referentiel.creerModele(code, libelle, heureDebut, heureFin, pause, toleranceRetard, toleranceDepart, nuit));
    }

    @PostMapping("/parametres/feries")
    public String ajouterFerie(@RequestParam(required = false) LocalDate jour, @RequestParam(required = false) String libelle,
                               RedirectAttributes flash) {
        return action(flash, "Jour férié ajouté.", "/rh/temps/parametres", () -> referentiel.ajouterFerie(jour, libelle));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/temps";
    }
}
