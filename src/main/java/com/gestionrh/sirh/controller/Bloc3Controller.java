// Bloc 3
package com.gestionrh.sirh.controller;

import java.math.BigDecimal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import com.gestionrh.sirh.model.ReponseCandidatModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.Bloc3Exception;
import com.gestionrh.sirh.service.PropositionForm;
import com.gestionrh.sirh.service.SelectionService;

@Controller
@RequestMapping("/bloc3")
@PreAuthorize("hasAnyRole('RH','DG')")
public class Bloc3Controller {
    private final SelectionService service;
    private final EmployeRepository employes;

    public Bloc3Controller(SelectionService service, EmployeRepository employes) {
        this.service = service;
        this.employes = employes;
    }

    /** Alias convivial pour l'adresse mentionnée dans le guide de test. */
    @GetMapping({"/solutions", "/solutions/"})
    public String solutions() {
        return "redirect:/bloc3/selections";
    }

    @ExceptionHandler(Bloc3Exception.class)
    public String erreur(Bloc3Exception e, HttpServletRequest request, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        String chemin = request.getRequestURI().substring(request.getContextPath().length());
        String prefixe = "/bloc3/selections/";
        if (chemin.startsWith(prefixe)) {
            String suite = chemin.substring(prefixe.length());
            String id = suite.split("/", 2)[0];
            if (id.matches("[0-9]+")) {
                return "redirect:" + prefixe + id;
            }
        }
        return "redirect:/bloc3/selections";
    }

    @GetMapping("/selections")
    public String selections(@RequestParam(required = false) String q,
                             @RequestParam(defaultValue = "recent") String tri,
                             @RequestParam(defaultValue = "0") int page,
                             Model model) {
        Integer idRecherche = null;
        if (q != null && !q.isBlank()) {
            try {
                idRecherche = Integer.valueOf(q.trim());
            } catch (NumberFormatException e) {
                idRecherche = -1;
            }
        }
        Sort sort = switch (tri) {
            case "candidat" -> Sort.by("idCandidatB2").ascending();
            case "offre" -> Sort.by("idOffreB2").ascending();
            case "rang" -> Sort.by("rangClassement").ascending().and(Sort.by("idSelection").descending());
            case "note" -> Sort.by(Sort.Order.desc("scoreFinal").nullsLast(), Sort.Order.desc("idSelection"));
            default -> Sort.by("idSelection").descending();
        };
        int pageValide = Math.max(0, page);
        model.addAttribute("selectionPage", service.listerSelections(idRecherche,
                PageRequest.of(pageValide, 10, sort)));
        model.addAttribute("q", q);
        model.addAttribute("tri", tri);
        return "bloc3/selections";
    }

    @GetMapping("/selections/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        preparerDetail(id, model);
        return "bloc3/detail";
    }

    @PostMapping("/selections/reception")
    @PreAuthorize("hasRole('RH')")
    public String recevoirDossier(@RequestParam Integer idCandidatureB2,
                                 @RequestParam Integer idOffreB2,
                                 @RequestParam Integer idCandidatB2,
                                 @RequestParam(required = false) BigDecimal scoreTest,
                                 @RequestParam(required = false) BigDecimal scoreEntretienRh,
                                 @RequestParam(required = false) BigDecimal scoreEntretienTech,
                                 @RequestParam(required = false) BigDecimal scoreEntretienDg,
                                 @RequestParam(required = false) BigDecimal scoreFinal,
                                 @RequestParam(required = false) Integer rangClassement,
                                 Authentication auth, RedirectAttributes flash) {
        var selection = service.recevoirDossier(idCandidatureB2, idOffreB2, idCandidatB2,
                scoreTest, scoreEntretienRh, scoreEntretienTech, scoreEntretienDg,
                scoreFinal, rangClassement, employe(auth).getId());
        flash.addFlashAttribute("succes", "Le dossier du Bloc 2 a été enregistré.");
        return redirectDetail(selection.getIdSelection());
    }

    @PostMapping("/selections/{id}/verification")
    @PreAuthorize("hasRole('RH')")
    public String verifier(@PathVariable Integer id,
                           @RequestParam String codeEtat,
                           @RequestParam(required = false) String elementsManquants,
                           Authentication auth, RedirectAttributes flash) {
        service.verifierDossier(id, codeEtat, elementsManquants, employe(auth).getId());
        flash.addFlashAttribute("succes", "La vérification du dossier a été enregistrée.");
        return redirectDetail(id);
    }

    @PostMapping("/selections/{id}/decision")
    @PreAuthorize("hasRole('RH')")
    public String decider(@PathVariable Integer id,
                          @RequestParam String codeDecision,
                          @RequestParam(required = false) String justification,
                          @RequestParam(required = false) Integer rangListeAttente,
                          @RequestParam(defaultValue = "false") boolean validationObligatoire,
                          Authentication auth, RedirectAttributes flash) {
        service.decider(id, codeDecision, justification, rangListeAttente,
                validationObligatoire, employe(auth).getId());
        flash.addFlashAttribute("succes", "La décision a été enregistrée.");
        return redirectDetail(id);
    }

    @PostMapping("/selections/{id}/validation")
    @PreAuthorize("hasAnyRole('RH','DG')")
    public String validerDecision(@PathVariable Integer id,
                                  @RequestParam boolean approuvee,
                                  @RequestParam(required = false) String commentaire,
                                  Authentication auth, RedirectAttributes flash) {
        service.validerDecision(id, approuvee, employe(auth).getId(), commentaire);
        flash.addFlashAttribute("succes", "La validation de la décision a été enregistrée.");
        return redirectDetail(id);
    }

    @PostMapping("/selections/{id}/propositions")
    @PreAuthorize("hasRole('RH')")
    public String preparerProposition(@PathVariable Integer id,
                                      @ModelAttribute PropositionForm form,
                                      Authentication auth, RedirectAttributes flash) {
        service.preparerProposition(id, form, employe(auth).getId());
        flash.addFlashAttribute("succes", "La proposition a été préparée. Vérifiez-la puis envoyez-la au candidat.");
        return redirectDetail(id);
    }

    @PostMapping("/propositions/{id}/envoyer")
    @PreAuthorize("hasRole('RH')")
    public String envoyerProposition(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        Integer selectionId = service.selectionPourProposition(id);
        service.envoyerProposition(id, employe(auth).getId());
        flash.addFlashAttribute("succes", "La proposition est marquée comme envoyée.");
        return redirectDetail(selectionId);
    }

    @PostMapping("/propositions/{id}/reponse")
    @PreAuthorize("hasRole('RH')")
    public String reponseCandidat(@PathVariable Integer id,
                                  @RequestParam String codeTypeReponse,
                                  @RequestParam(required = false) String commentaire,
                                  Authentication auth, RedirectAttributes flash) {
        Integer selectionId = service.selectionPourProposition(id);
        service.enregistrerReponse(id, codeTypeReponse, commentaire, employe(auth).getId());
        flash.addFlashAttribute("succes", "La réponse du candidat a été enregistrée.");
        return redirectDetail(selectionId);
    }

    @PostMapping("/selections/{id}/confirmer")
    @PreAuthorize("hasRole('RH')")
    public String confirmer(@PathVariable Integer id,
                            @RequestParam Integer nombrePostes,
                            Authentication auth, RedirectAttributes flash) {
        service.confirmerRecrutement(id, nombrePostes, employe(auth).getId());
        flash.addFlashAttribute("succes", "Le recrutement est confirmé. Le transfert au Bloc 4 peut être suivi ci-dessous.");
        return redirectDetail(id);
    }

    @PostMapping("/selections/{id}/transfert")
    @PreAuthorize("hasRole('RH')")
    public String transfert(@PathVariable Integer id,
                            @RequestParam boolean transmis,
                            @RequestParam(required = false) String referenceBloc4,
                            @RequestParam(required = false) String erreurTransfert,
                            Authentication auth, RedirectAttributes flash) {
        service.enregistrerTransfert(id, transmis, referenceBloc4, erreurTransfert, employe(auth).getId());
        flash.addFlashAttribute("succes", transmis
                ? "Le transfert au Bloc 4 a été marqué comme réussi."
                : "L'échec du transfert a été enregistré ; vous pouvez réessayer.");
        return redirectDetail(id);
    }

    private void preparerDetail(Integer id, Model model) {
        var propositions = service.propositions(id);
        Map<Integer, List<ReponseCandidatModel>> reponsesParProposition = new LinkedHashMap<>();
        for (var proposition : propositions) {
            reponsesParProposition.put(proposition.getIdProposition(),
                    service.reponses(proposition.getIdProposition()));
        }
        model.addAttribute("selection", service.selection(id));
        model.addAttribute("propositions", propositions);
        model.addAttribute("reponsesParProposition", reponsesParProposition);
        model.addAttribute("recrutement", service.recrutement(id));
        model.addAttribute("historique", service.historique(id));
        model.addAttribute("etatsDossier", service.etatsDossier());
        model.addAttribute("decisions", service.decisions());
        model.addAttribute("typesContrat", service.typesContrat());
        model.addAttribute("typesReponse", service.typesReponse());
        model.addAttribute("propositionForm", new PropositionForm());
    }

    private EmployeModel employe(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc3Exception("Votre compte ne correspond à aucun employé actif."));
    }

    private static String redirectDetail(Integer id) {
        return "redirect:/bloc3/selections/" + id;
    }
}
