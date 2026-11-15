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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.Bloc4Exception;
import com.gestionrh.sirh.service.DocumentService;
import com.gestionrh.sirh.service.DossierDocumentService;
import com.gestionrh.sirh.service.SalarieService;
import com.gestionrh.sirh.service.dto.DocumentForm;

/**
 * Bloc 5 §17.3 — « Mon dossier » : le salarié consulte son propre dossier (RG-AC01, RG-IP07)
 * et dépose les pièces demandées ou à renouveler.
 */
@Controller
@RequestMapping("/mon-dossier")
public class MonDossierController {

    private final SalarieService salaries;
    private final DocumentService documents;
    private final DossierDocumentService pieces;
    private final EmployeRepository employes;

    public MonDossierController(SalarieService salaries, DocumentService documents,
                                DossierDocumentService pieces, EmployeRepository employes) {
        this.salaries = salaries;
        this.documents = documents;
        this.pieces = pieces;
        this.employes = employes;
    }

    @GetMapping
    public String consulter(@RequestParam(required = false) Integer piece, Authentication auth, Model model) {
        EmployeModel moi = moi(auth);
        if (moi == null) {
            model.addAttribute("sansEmploye", true);
            return "mon-dossier";
        }
        model.addAttribute("detail", salaries.detail(moi.getId(), "SELF"));
        model.addAttribute("typesPieces", pieces.typesActifs());
        model.addAttribute("pieceChoisie", piece);
        return "mon-dossier";
    }

    @PostMapping("/documents")
    public String deposer(@ModelAttribute DocumentForm form, @RequestParam("fichier") MultipartFile fichier,
                          Authentication auth, RedirectAttributes flash) {
        EmployeModel moi = moi(auth);
        if (moi == null) {
            return "redirect:/mon-dossier";
        }
        if (form.getIdTypeAdministratif() == null) {
            flash.addFlashAttribute("erreur", "Choisissez la pièce que vous déposez.");
            return "redirect:/mon-dossier";
        }
        // Le salarié ne dépose que dans son propre dossier, jamais sur un contrat ou une tâche.
        form.setIdEmploye(moi.getId());
        form.setIdContrat(null);
        form.setIdOnboardingTache(null);
        try {
            documents.enregistrer(fichier, form, moi);
            flash.addFlashAttribute("succes", "Pièce déposée : elle sera vérifiée par le RH.");
        } catch (Bloc4Exception e) {
            flash.addFlashAttribute("erreur", e.getMessage());
        }
        return "redirect:/mon-dossier";
    }

    @GetMapping("/documents/{id}")
    public ResponseEntity<Resource> telecharger(@PathVariable Integer id, Authentication auth) {
        EmployeModel moi = moi(auth);
        if (moi == null) {
            throw new Bloc4Exception("Votre compte ne correspond à aucun employé.");
        }
        var document = documents.detail(id);
        if (document.getIdEmploye() == null || !document.getIdEmploye().equals(moi.getId())) {
            return ResponseEntity.status(403).build();
        }
        Resource ressource = documents.charger(id, moi);
        String nom = document.getNomFichier() == null ? "document" : document.getNomFichier();
        MediaType type = document.getTypeMime() == null ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(document.getTypeMime());
        return ResponseEntity.ok().contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nom, StandardCharsets.UTF_8).build().toString())
                .body(ressource);
    }

    private EmployeModel moi(Authentication auth) {
        return auth == null ? null : employes.trouverParLogin(auth.getName()).orElse(null);
    }
}
