package com.gestionrh.sirh.controller.recrutement;

import com.gestionrh.sirh.enums.recrutement.OffreStatut;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.service.recrutement.OffreService;
import com.gestionrh.sirh.service.recrutement.RattachementCandidatureService;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rh/recrutement/reserve")
@PreAuthorize("hasRole('RH')")
public class ReserveCandidatureController {

    private final RattachementCandidatureService reserve;
    private final OffreService offres;

    public ReserveCandidatureController(RattachementCandidatureService reserve, OffreService offres) {
        this.reserve = reserve;
        this.offres = offres;
    }

    @GetMapping
    public String liste(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", reserve.rechercherReserve(q,
                PageRequest.of(Math.max(page, 0), 20, Sort.by(Sort.Direction.ASC, "dateReception"))));
        model.addAttribute("q", q);
        model.addAttribute("offres", offres.rechercher("", OffreStatut.PUBLIEE,
                PageRequest.of(0, 100, Sort.by(Sort.Direction.ASC, "dateLimite"))).getContent().stream()
                .filter(offres::estOuverte).toList());
        model.addAttribute("titrePage", "Réserve de candidatures spontanées");
        return "recrutement/candidature/reserve";
    }

    @PostMapping("/{id}/rattacher")
    public String rattacher(@PathVariable Integer id, @RequestParam Integer offreId,
            RedirectAttributes ra) {
        try {
            reserve.rattacher(id, offreId);
            ra.addFlashAttribute("message", "Candidature rattachée et classée.");
        } catch (RegleMetierException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("erreur", "Le candidat possède déjà une candidature active pour cette offre.");
        }
        return "redirect:/rh/recrutement/reserve";
    }
}
