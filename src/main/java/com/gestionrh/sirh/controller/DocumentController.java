package com.gestionrh.sirh.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.DocumentService;
import com.gestionrh.sirh.service.dto.DocumentForm;

@Controller
@RequestMapping("/rh/documents")
public class DocumentController {

    private final DocumentService service;
    private final EmployeRepository employes;
    private final ContratRepository contrats;

    public DocumentController(DocumentService service, EmployeRepository employes,
                              ContratRepository contrats) {
        this.service = service;
        this.employes = employes;
        this.contrats = contrats;
    }

    @GetMapping
    public String liste(@RequestParam(required = false) String type,
                        @RequestParam(required = false) String statut,
                        @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("documents", service.lister(type, statut, page, 15));
        model.addAttribute("type", type);
        model.addAttribute("statut", statut);
        return "rh/documents/liste";
    }

    @GetMapping("/nouveau")
    public String nouveau(@RequestParam(required = false) Integer idEmploye,
                          @RequestParam(required = false) Integer idContrat,
                          @RequestParam(required = false) Integer idTache, Model model) {
        DocumentForm form = new DocumentForm();
        form.setIdEmploye(idEmploye);
        form.setIdContrat(idContrat);
        form.setIdOnboardingTache(idTache);
        model.addAttribute("form", form);
        referentiels(model);
        return "rh/documents/formulaire";
    }

    @PostMapping
    public String televerser(@ModelAttribute("form") DocumentForm form,
                             @RequestParam("fichier") MultipartFile fichier,
                             Authentication auth, Model model, RedirectAttributes flash) {
        try {
            var document = service.enregistrer(fichier, form, moi(auth));
            flash.addFlashAttribute("succes", "Document déposé.");
            return "redirect:/rh/documents";
        } catch (Bloc4Exception e) {
            model.addAttribute("erreurs", java.util.List.of(e.getMessage()));
            model.addAttribute("form", form);
            referentiels(model);
            return "rh/documents/formulaire";
        }
    }

    @GetMapping("/{id}/telecharger")
    public ResponseEntity<Resource> telecharger(@PathVariable Integer id, Authentication auth) {
        var document = service.detail(id);
        Resource ressource = service.charger(id, moi(auth));
        String nom = document.getNomFichier() == null ? "document" : document.getNomFichier();
        MediaType mediaType = document.getTypeMime() == null ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(document.getTypeMime());
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nom, StandardCharsets.UTF_8).build().toString())
                .body(ressource);
    }

    @PostMapping("/{id}/verifier")
    public String verifier(@PathVariable Integer id, Authentication auth, RedirectAttributes flash) {
        service.verifier(id, moi(auth));
        flash.addFlashAttribute("succes", "Document vérifié.");
        return "redirect:/rh/documents";
    }

    @PostMapping("/{id}/rejeter")
    public String rejeter(@PathVariable Integer id, @RequestParam String motif,
                          Authentication auth, RedirectAttributes flash) {
        service.rejeter(id, motif, moi(auth));
        flash.addFlashAttribute("succes", "Document rejeté.");
        return "redirect:/rh/documents";
    }

    @ExceptionHandler(Bloc4Exception.class)
    public String refus(Bloc4Exception e, RedirectAttributes flash) {
        flash.addFlashAttribute("erreur", e.getMessage());
        return "redirect:/rh/documents";
    }

    private void referentiels(Model model) {
        model.addAttribute("employes", employes.findByActifTrueOrderByNomAsc());
        model.addAttribute("contrats", contrats.findAll());
    }

    private EmployeModel moi(Authentication auth) {
        return employes.trouverParLogin(auth.getName())
                .orElseThrow(() -> new Bloc4Exception("Votre compte ne correspond à aucun employé."));
    }
}
