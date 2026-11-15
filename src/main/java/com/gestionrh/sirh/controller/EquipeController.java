package com.gestionrh.sirh.controller;

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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.DemandeCongeModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.AbsenceRepository;
import com.gestionrh.sirh.repository.DemandeCongeRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.AnomalieService;
import com.gestionrh.sirh.service.CongeService;
import com.gestionrh.sirh.service.ManagerService;

/** Blocs 6 et 7 — espace manager : anomalies à traiter, congés à valider, calendrier d'équipe. */
@Controller
@RequestMapping("/equipe")
public class EquipeController extends EspaceTempsController {

    private final AnomalieService anomalies;
    private final CongeService conges;
    private final DemandeCongeRepository demandes;
    private final AbsenceRepository absences;

    public EquipeController(EmployeRepository employes, ManagerService managers, AnomalieService anomalies,
                            CongeService conges, DemandeCongeRepository demandes, AbsenceRepository absences) {
        super(employes, managers);
        this.anomalies = anomalies;
        this.conges = conges;
        this.demandes = demandes;
        this.absences = absences;
    }

    @GetMapping
    public String page(Authentication auth, Model model) {
        EmployeModel moi = moiObligatoire(auth);
        List<EmployeModel> equipe = managers.equipeDe(moi);
        model.addAttribute("equipe", equipe);
        model.addAttribute("anomalies", anomalies.aTraiterPar(moi, false));
        List<DemandeCongeModel> aValider = conges.aValiderParManager(moi);
        model.addAttribute("demandes", aValider);
        Map<Integer, List<String>> simultanes = new LinkedHashMap<>();
        aValider.forEach(d -> simultanes.put(d.getId(), conges.absentsSimultanes(d)));
        model.addAttribute("simultanes", simultanes);
        // Calendrier des 30 prochains jours (RG-AB07 : le manager ne voit pas le motif médical).
        LocalDate debut = LocalDate.now(), fin = debut.plusDays(30);
        List<Integer> ids = equipe.stream().map(EmployeModel::getId).toList();
        model.addAttribute("congesEquipe", demandes.toutesValideesEntre(debut, fin).stream()
                .filter(d -> ids.contains(d.getEmploye().getId())).toList());
        model.addAttribute("absencesEquipe", absences.toutesActivesEntre(debut, fin).stream()
                .filter(a -> ids.contains(a.getEmploye().getId())).toList());
        return "equipe";
    }

    @PostMapping("/anomalies/{id}/accepter")
    public String accepter(@PathVariable Integer id, @RequestParam(required = false) String commentaire,
                           Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Régularisation acceptée : la journée est recalculée.", "/equipe",
                () -> anomalies.accepter(id, commentaire, moi, false));
    }

    @PostMapping("/anomalies/{id}/refuser")
    public String refuser(@PathVariable Integer id, @RequestParam(required = false) String motif,
                          Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Anomalie confirmée.", "/equipe", () -> anomalies.refuser(id, motif, moi, false));
    }

    @PostMapping("/conges/{id}/valider")
    public String validerConge(@PathVariable Integer id, @RequestParam(required = false) String commentaire,
                               Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Demande validée : elle passe au RH.", "/equipe",
                () -> conges.validerManager(id, commentaire, moi, false));
    }

    @PostMapping("/conges/{id}/refuser")
    public String refuserConge(@PathVariable Integer id, @RequestParam(required = false) String motif,
                               Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moiObligatoire(auth);
        return action(flash, "Demande refusée.", "/equipe", () -> conges.refuser(id, motif, moi, false));
    }
}
