package com.gestionrh.sirh.controller.recrutement;

import com.gestionrh.sirh.dto.recrutement.DecisionCandidatureForm;
import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.enums.recrutement.TypeCv;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.recrutement.DecisionCandidatureService;
import com.gestionrh.sirh.service.recrutement.SuiviCandidatureService;
import com.gestionrh.sirh.service.recrutement.StockageCvService;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rh/recrutement/candidatures")
@PreAuthorize("hasRole('RH')")
public class SuiviCandidatureController {

    private static final int TAILLE_PAGE = 20;
    private static final Set<String> TRIS_AUTORISES = Set.of("dateReception", "numero", "statut", "classementAuto");
    private final SuiviCandidatureService suivi;
    private final DecisionCandidatureService decisions;
    private final EmployeRepository employes;
    private final StockageCvService stockageCv;

    public SuiviCandidatureController(SuiviCandidatureService suivi, DecisionCandidatureService decisions,
            EmployeRepository employes, StockageCvService stockageCv) {
        this.suivi = suivi;
        this.decisions = decisions;
        this.employes = employes;
        this.stockageCv = stockageCv;
    }

    @GetMapping
    public String liste(@RequestParam(required = false) Integer offreId,
            @RequestParam(required = false) Classement classement,
            @RequestParam(required = false) CandidatureStatut statut,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "dateReception") String tri,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(defaultValue = "0") int page, Model model) {
        String propriete = TRIS_AUTORISES.contains(tri) ? tri : "dateReception";
        Sort.Direction sens;
        try { sens = Sort.Direction.fromString(direction); } catch (IllegalArgumentException e) { sens = Sort.Direction.DESC; }
        model.addAttribute("page", suivi.rechercher(offreId, classement, statut, q,
                PageRequest.of(Math.max(0, page), TAILLE_PAGE, Sort.by(sens, propriete))));
        model.addAttribute("offres", suivi.toutesLesOffres());
        model.addAttribute("classements", Classement.values());
        model.addAttribute("statuts", CandidatureStatut.values());
        model.addAttribute("offreId", offreId);
        model.addAttribute("classement", classement);
        model.addAttribute("statut", statut);
        model.addAttribute("q", q);
        model.addAttribute("tri", propriete);
        model.addAttribute("direction", sens.name());
        model.addAttribute("indicateurs", suivi.indicateurs());
        model.addAttribute("titrePage", "Suivi des candidatures");
        return "recrutement/candidature/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        var candidature = suivi.detail(id);
        model.addAttribute("candidature", candidature);
        model.addAttribute("peutDecider", candidature.statut() == CandidatureStatut.A_ETUDIER);
        model.addAttribute("classementARegarder", candidature.classementAuto() == Classement.A_REGARDER);
        model.addAttribute("historique", suivi.historique(id));
        model.addAttribute("decisionForm", new DecisionCandidatureForm());
        model.addAttribute("classementsFinaux", List.of(Classement.CONFORME, Classement.NON_CONFORME));
        model.addAttribute("titrePage", "Candidature");
        return "recrutement/candidature/detail";
    }

    @PostMapping("/{id}/valider")
    public String validerAutomatique(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        try {
            decisions.validerAutomatique(id, idEmploye(auth));
            ra.addFlashAttribute("message", "Classement automatique confirmé.");
        } catch (RegleMetierException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/rh/recrutement/candidatures/" + id;
    }

    @PostMapping("/{id}/decider")
    public String decider(@PathVariable Integer id, @Valid DecisionCandidatureForm form,
            BindingResult result, Authentication auth, RedirectAttributes ra) {
        if (result.hasErrors()) {
            ra.addFlashAttribute("erreur", "Le commentaire dépasse la longueur autorisée.");
            return "redirect:/rh/recrutement/candidatures/" + id;
        }
        try {
            decisions.decider(id, form.getClassement(), form.getCommentaire(), idEmploye(auth));
            ra.addFlashAttribute("message", "Décision enregistrée.");
        } catch (RegleMetierException e) {
            ra.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/rh/recrutement/candidatures/" + id;
    }

    @GetMapping("/{id}/cv")
    public ResponseEntity<Resource> telechargerCv(@PathVariable Integer id) throws IOException {
        Candidature c = suivi.chargerAvecCandidat(id);
        Resource resource = new FileSystemResource(stockageCv.cheminControle(c.getCvChemin()));
        String type = c.getCvType() == TypeCv.PDF ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(c.getCvNomFichier(), StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(type))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString()).body(resource);
    }

    private Integer idEmploye(Authentication auth) {
        EmployeModel employe = employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new RegleMetierException("Le compte RH n'est pas associé à un employé."));
        return employe.getId();
    }
}
