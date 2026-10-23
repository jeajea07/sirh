package com.gestionrh.sirh.controller.recrutement;

import com.gestionrh.sirh.dto.recrutement.CandidatureForm;
import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.service.recrutement.CandidatureService;
import com.gestionrh.sirh.service.recrutement.OffreService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/candidatures")
public class CandidaturePubliqueController {

    private final CandidatureService candidatures;
    private final OffreService offres;

    public CandidaturePubliqueController(CandidatureService candidatures, OffreService offres) {
        this.candidatures = candidatures;
        this.offres = offres;
    }

    @GetMapping("/nouvelle")
    public String nouveau(@RequestParam(required = false) Integer offreId, Model model) {
        preparer(model, offreId, new CandidatureForm());
        return "recrutement/candidature/formulaire-public";
    }

    @PostMapping("/nouvelle")
    public String deposer(@RequestParam(required = false) Integer offreId,
            @Valid @ModelAttribute("candidatureForm") CandidatureForm form, BindingResult result, Model model) {
        if (form.getFichierCv() == null || form.getFichierCv().isEmpty()) {
            result.rejectValue("fichierCv", "NotNull", "Veuillez joindre votre CV en PDF ou DOCX.");
        }
        if (!result.hasErrors()) {
            try {
                Candidature candidature = candidatures.recevoir(offreId, form);
                model.addAttribute("numeroCandidature", candidature.getNumero());
                return "recrutement/candidature/confirmation";
            } catch (RegleMetierException e) {
                model.addAttribute("erreur", e.getMessage());
            } catch (DataIntegrityViolationException e) {
                model.addAttribute("erreur", "Une candidature existe déjà pour cette offre et cette adresse e-mail.");
            }
        }
        preparer(model, offreId, form);
        return "recrutement/candidature/formulaire-public";
    }

    private void preparer(Model model, Integer offreId, CandidatureForm form) {
        String titreOffre = null;
        if (offreId != null) {
            try {
                titreOffre = offres.trouverOuverte(offreId).getTitre();
            } catch (RegleMetierException e) {
                model.addAttribute("erreur", e.getMessage());
            }
        }
        model.addAttribute("offreId", offreId);
        model.addAttribute("titreOffre", titreOffre);
        model.addAttribute("candidatureForm", form);
        model.addAttribute("niveaux", NiveauEtude.values());
        model.addAttribute("titrePage", titreOffre == null ? "Candidature spontanée" : "Postuler à " + titreOffre);
    }
}
