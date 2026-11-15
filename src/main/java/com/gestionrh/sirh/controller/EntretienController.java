package com.gestionrh.sirh.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.EntretienModel;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.EntretienRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.service.recrutement.EntretienService;
import com.gestionrh.sirh.service.recrutement.EntretienService.QuestionGrille;

/** Bloc 2 — entretiens RH, Technicien et DG (planification, notes, confirmation RH). */
@Controller
@RequestMapping("/rh/entretiens")
@PreAuthorize("hasRole('RH')")
public class EntretienController {

    /** Ligne de la liste : une candidature à son niveau d'entretien courant. */
    public record LigneEntretien(Integer idCandidature, String numero, String candidat, String offre,
            int niveau, String niveauLibelle, EntretienModel entretien, String etape) { }

    private final EntretienService service;
    private final EntretienRepository entretiens;
    private final CandidatureRepository candidatures;
    private final EmployeRepository employes;

    public EntretienController(EntretienService service, EntretienRepository entretiens,
            CandidatureRepository candidatures, EmployeRepository employes) {
        this.service = service;
        this.entretiens = entretiens;
        this.candidatures = candidatures;
        this.employes = employes;
    }

    @ExceptionHandler(RegleMetierException.class)
    public String refus(RegleMetierException e, RedirectAttributes flash,
            jakarta.servlet.http.HttpServletRequest request) {
        flash.addFlashAttribute("erreur", e.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null && referer.contains("/rh/entretiens") ? referer.replaceAll("^https?://[^/]+", "") : "/rh/entretiens");
    }

    @GetMapping
    public String liste(Model model) {
        List<Candidature> enCours = candidatures.findByStatutInOrderByDateValidationAsc(EntretienService.STATUTS_ENTRETIEN);
        Map<String, EntretienModel> parCle = new HashMap<>();
        if (!enCours.isEmpty()) {
            for (EntretienModel e : entretiens.findByIdCandidatureIn(enCours.stream().map(Candidature::getIdCandidature).toList())) {
                parCle.put(e.getIdCandidature() + "-" + e.getNiveau(), e);
            }
        }
        List<LigneEntretien> lignes = new ArrayList<>();
        for (Candidature c : enCours) {
            int niveau = EntretienService.niveauDuStatut(c.getStatut());
            EntretienModel e = parCle.get(c.getIdCandidature() + "-" + niveau);
            String etape = e == null ? "A planifier"
                    : e.isADecider() ? "A confirmer"
                    : e.isModifiable() ? "Planifié" : e.getStatut();
            lignes.add(new LigneEntretien(c.getIdCandidature(), c.getNumero(),
                    c.getCandidat().getPrenom() + " " + c.getCandidat().getNom(),
                    c.getOffre() == null ? "—" : c.getOffre().getTitre(),
                    niveau, EntretienModel.libelleNiveau(niveau), e, etape));
        }
        model.addAttribute("lignes", lignes);
        return "rh/entretiens/liste";
    }

    @GetMapping("/candidature/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Candidature c = candidatures.findWithCandidatAndOffreByIdCandidature(id)
                .orElseThrow(() -> new RegleMetierException("Candidature introuvable."));
        List<EntretienModel> liste = service.entretiensDe(id);
        int niveauCourant = EntretienService.niveauDuStatut(c.getStatut());
        EntretienModel courant = liste.stream().filter(e -> e.getNiveau() == niveauCourant).findFirst().orElse(null);
        model.addAttribute("candidature", c);
        model.addAttribute("entretiens", liste);
        model.addAttribute("niveauCourant", niveauCourant);
        model.addAttribute("niveauCourantLibelle", niveauCourant == 0 ? "" : EntretienModel.libelleNiveau(niveauCourant));
        model.addAttribute("courant", courant);
        model.addAttribute("grilleDefaut", niveauCourant == 0 ? List.of() : EntretienService.grilleParDefaut(niveauCourant));
        model.addAttribute("resume", service.resume(id));
        model.addAttribute("dateMin", LocalDateTime.now().withSecond(0).withNano(0).toString());
        model.addAttribute("estConfirmationFinale", c.getStatut() == CandidatureStatut.CONFIRMATION_FINALE);
        return "rh/entretiens/detail";
    }

    @PostMapping("/candidature/{id}/planifier")
    public String planifier(@PathVariable Integer id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(required = false) String lieu, @RequestParam(required = false) String jury,
            @RequestParam BigDecimal seuil, @RequestParam Map<String, String> champs,
            Authentication auth, RedirectAttributes flash) {
        List<QuestionGrille> grille = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String question = champs.get("question_" + i);
            if (question == null || question.isBlank()) continue;
            int importance = entier(champs.get("importance_" + i), 1);
            boolean eliminatoire = champs.containsKey("eliminatoire_" + i);
            BigDecimal min = decimal(champs.get("min_" + i));
            grille.add(new QuestionGrille(question, importance, eliminatoire, min));
        }
        EntretienModel e = service.planifier(id, date, lieu, jury, seuil, grille, moi(auth));
        flash.addFlashAttribute("succes", e.getNiveauLibelle() + " planifié. Le candidat a reçu sa convocation.");
        return "redirect:/rh/entretiens/candidature/" + id;
    }

    @PostMapping("/{idEntretien}/reporter")
    public String reporter(@PathVariable Integer idEntretien,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(required = false) String motif, Authentication auth, RedirectAttributes flash) {
        service.reporter(idEntretien, date, motif, moi(auth));
        flash.addFlashAttribute("succes", "Entretien reporté. Le rendez-vous d'origine est conservé dans l'historique.");
        return retour(idEntretien);
    }

    @PostMapping("/{idEntretien}/absent")
    public String absent(@PathVariable Integer idEntretien, Authentication auth, RedirectAttributes flash) {
        service.marquerAbsent(idEntretien, moi(auth));
        flash.addFlashAttribute("succes", "Absence enregistrée : résultat proposé « non admis ».");
        return retour(idEntretien);
    }

    @PostMapping("/{idEntretien}/notes")
    public String noter(@PathVariable Integer idEntretien, @RequestParam Map<String, String> champs,
            Authentication auth, RedirectAttributes flash) {
        Map<Integer, BigDecimal> notes = new HashMap<>();
        champs.forEach((k, v) -> {
            if (k.startsWith("note_")) {
                BigDecimal d = decimal(v);
                if (d != null) notes.put(Integer.valueOf(k.substring(5)), d);
            }
        });
        EntretienModel e = service.noter(idEntretien, notes, moi(auth));
        flash.addFlashAttribute("succes", "Notes enregistrées : " + e.getNoteGenerale() + "/5 — résultat proposé : "
                + (EntretienModel.ADMIS.equals(e.getResultatPropose()) ? "admis" : "non admis") + ".");
        return retour(idEntretien);
    }

    @PostMapping("/{idEntretien}/decision")
    public String decider(@PathVariable Integer idEntretien, @RequestParam String decision,
            @RequestParam(required = false) String motif, Authentication auth, RedirectAttributes flash) {
        boolean admis = "admis".equalsIgnoreCase(decision);
        CandidatureStatut statut = service.decider(idEntretien, admis, motif, moi(auth));
        flash.addFlashAttribute("succes", switch (statut) {
            case ENTRETIEN_TECHNICIEN -> "Candidat admis : il passe à l'entretien 2 (Technicien).";
            case ENTRETIEN_DG -> "Candidat admis : il passe à l'entretien 3 (DG).";
            case CONFIRMATION_FINALE -> "Entretiens réussis : le dossier est prêt pour la confirmation finale (Sélection et embauche).";
            default -> "Candidature non retenue. Le candidat a été prévenu.";
        });
        return retour(idEntretien);
    }

    private String retour(Integer idEntretien) {
        return "redirect:/rh/entretiens/candidature/" + service.charger(idEntretien).getIdCandidature();
    }

    private Integer moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new RegleMetierException("Le compte RH n'est pas associé à un employé.")).getId();
    }

    private static int entier(String v, int defaut) {
        try { return v == null || v.isBlank() ? defaut : Integer.parseInt(v.trim()); }
        catch (NumberFormatException e) { return defaut; }
    }

    private static BigDecimal decimal(String v) {
        try { return v == null || v.isBlank() ? null : new BigDecimal(v.trim().replace(',', '.')); }
        catch (NumberFormatException e) { return null; }
    }
}
