package com.gestionrh.sirh.controller.recrutement;

import com.gestionrh.sirh.dto.recrutement.CritereForm;
import com.gestionrh.sirh.dto.recrutement.OffreForm;
import com.gestionrh.sirh.service.recrutement.OffreDepuisDemandeService;
import com.gestionrh.sirh.enums.recrutement.NatureCritere;
import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import com.gestionrh.sirh.enums.recrutement.OffreStatut;
import com.gestionrh.sirh.enums.recrutement.TypeCritere;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.recrutement.OffreService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recrutement/offres")
@PreAuthorize("hasRole('RH')")
public class OffreController {

    private static final int TAILLE_PAGE = 10;
    private static final String VUE_LISTE = "recrutement/offre/liste";
    private static final String VUE_FORMULAIRE = "recrutement/offre/formulaire";
    private static final String VUE_DETAIL = "recrutement/offre/detail";
    private static final String VUE_CHOIX_DEMANDE = "recrutement/offre/choix-demande";

    private final OffreService service;
    private final OffreDepuisDemandeService offreDepuisDemande;
    private final EmployeRepository employes;

    public OffreController(OffreService service, OffreDepuisDemandeService offreDepuisDemande, EmployeRepository employes) {
        this.service = service;
        this.offreDepuisDemande = offreDepuisDemande;
        this.employes = employes;
    }

    // ------------------------------------------------------------ liste

    @GetMapping
    public String liste(@RequestParam(defaultValue = "") String q,
                        @RequestParam(required = false) OffreStatut statut,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), TAILLE_PAGE, Sort.by(Sort.Direction.DESC, "dateCreation"));
        model.addAttribute("page", service.rechercher(q, statut, pageable));
        model.addAttribute("q", q);
        model.addAttribute("statut", statut);
        model.addAttribute("statuts", OffreStatut.values());
        model.addAttribute("titrePage", "Offres d'emploi");
        return VUE_LISTE;
    }

    // ------------------------------------------------------------ creation

    @GetMapping("/nouveau")
    public String nouveau(Model model) {
        model.addAttribute("demandesAcceptees", offreDepuisDemande.demandesAccepteesSansOffre());
        model.addAttribute("titrePage", "Créer une offre depuis une demande acceptée");
        return VUE_CHOIX_DEMANDE;
    }

    @PostMapping("/nouveau")
    public String creerDepuisDemande(@RequestParam Integer idDemandeBesoin, RedirectAttributes ra) {
        try {
            Offre offre = offreDepuisDemande.creerDepuisDemande(idDemandeBesoin);
            ra.addFlashAttribute("message", "Offre créée depuis la demande acceptée : " + offre.getReference());
            return "redirect:/recrutement/offres/" + offre.getIdOffre();
        } catch (RegleMetierException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/recrutement/offres/nouveau";
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("erreur", "La demande ne peut pas être utilisée pour créer cette offre.");
            return "redirect:/recrutement/offres/nouveau";
        }
    }

    // ------------------------------------------------------------ detail

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Offre offre = service.trouver(id);
        model.addAttribute("offre", offre);
        model.addAttribute("obligatoires", service.criteres(id, TypeCritere.OBLIGATOIRE));
        model.addAttribute("souhaites", service.criteres(id, TypeCritere.SOUHAITE));
        if (offre.getStatut() == OffreStatut.BROUILLON || offre.getStatut() == OffreStatut.VALIDEE) {
            model.addAttribute("manquants", service.champsManquants(offre));
        }
        model.addAttribute("critereForm", new CritereForm());
        model.addAttribute("types", TypeCritere.values());
        model.addAttribute("natures", NatureCritere.values());
        model.addAttribute("niveaux", NiveauEtude.values());
        model.addAttribute("titrePage", offre.getReference());
        model.addAttribute("offreOuverte", service.estOuverte(offre));
        model.addAttribute("offreFermeeParDate", offre.getStatut() == OffreStatut.PUBLIEE && !service.estOuverte(offre));
        return VUE_DETAIL;
    }

    // ------------------------------------------------------------ modification

    @GetMapping("/{id}/modifier")
    public String modifier(@PathVariable Integer id, Model model) {
        Offre offre = service.trouver(id);
        preparerFormulaire(model, OffreForm.depuis(offre), offre);
        return VUE_FORMULAIRE;
    }

    @PostMapping("/{id}/modifier")
    public String enregistrer(@PathVariable Integer id,
                              @Valid @ModelAttribute("offreForm") OffreForm form, BindingResult result,
                              Model model, RedirectAttributes ra) {
        if (!result.hasErrors()) {
            try {
                service.modifier(id, form);
                ra.addFlashAttribute("message", "Offre modifiee.");
                return "redirect:/recrutement/offres/" + id;
            } catch (RegleMetierException e) {
                model.addAttribute("erreur", e.getMessage());
            }
        }
        preparerFormulaire(model, form, service.trouver(id));
        return VUE_FORMULAIRE;
    }

    // ------------------------------------------------------------ actions (cycle de vie)

    @PostMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            service.supprimer(id);
            ra.addFlashAttribute("message", "Offre supprimee.");
            return "redirect:/recrutement/offres";
        } catch (RegleMetierException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
            return "redirect:/recrutement/offres/" + id;
        }
    }

    @PostMapping("/{id}/valider")
    public String valider(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        return action(id, ra, "Offre validée.", () -> service.valider(id, utilisateurCourant(auth)));
    }

    @PostMapping("/{id}/publier")
    public String publier(@PathVariable Integer id, RedirectAttributes ra) {
        return action(id, ra, "Offre publiee sur le site vitrine.", () -> service.publier(id));
    }

    @PostMapping("/{id}/suspendre")
    public String suspendre(@PathVariable Integer id, RedirectAttributes ra) {
        return action(id, ra, "Offre suspendue.", () -> service.suspendre(id));
    }

    @PostMapping("/{id}/reprendre")
    public String reprendre(@PathVariable Integer id, RedirectAttributes ra) {
        return action(id, ra, "Offre republiee.", () -> service.reprendre(id));
    }

    @PostMapping("/{id}/terminer")
    public String terminer(@PathVariable Integer id, RedirectAttributes ra) {
        return action(id, ra, "Offre terminee.", () -> service.terminer(id));
    }

    @PostMapping("/{id}/annuler")
    public String annuler(@PathVariable Integer id, RedirectAttributes ra) {
        return action(id, ra, "Offre annulee.", () -> service.annuler(id));
    }

    // ------------------------------------------------------------ criteres

    @PostMapping("/{id}/criteres")
    public String ajouterCritere(@PathVariable Integer id, @ModelAttribute CritereForm form, RedirectAttributes ra) {
        return action(id, ra, "Critere ajoute.", () -> service.ajouterCritere(id, form));
    }

    @PostMapping("/{id}/criteres/{idCritere}/supprimer")
    public String supprimerCritere(@PathVariable Integer id, @PathVariable Integer idCritere, RedirectAttributes ra) {
        return action(id, ra, "Critere supprime.", () -> service.supprimerCritere(id, idCritere));
    }

    // ------------------------------------------------------------ outils

    private String action(Integer id, RedirectAttributes ra, String succes, Runnable operation) {
        try {
            operation.run();
            ra.addFlashAttribute("message", succes);
        } catch (RegleMetierException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/recrutement/offres/" + id;
    }

    private void preparerFormulaire(Model model, OffreForm form, Offre offre) {
        model.addAttribute("offreForm", form);
        model.addAttribute("offre", offre); // null en creation
        model.addAttribute("titrePage", offre == null ? "Nouvelle offre" : "Modifier " + offre.getReference());
    }

    private Integer utilisateurCourant(Authentication auth) {
        EmployeModel employe = employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new RegleMetierException("Le compte RH n'est pas associé à un employé."));
        return employe.getId();
    }
}
