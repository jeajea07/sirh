package com.gestionrh.sirh.controller.recrutement;

import com.gestionrh.sirh.dto.recrutement.CritereOffrePublic;
import com.gestionrh.sirh.dto.recrutement.OffrePublique;
import com.gestionrh.sirh.enums.recrutement.TypeCritere;
import com.gestionrh.sirh.model.recrutement.CritereOffre;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.service.recrutement.OffreService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/offres")
public class VitrineOffreController {

    private final OffreService offres;

    public VitrineOffreController(OffreService offres) {
        this.offres = offres;
    }

    @GetMapping
    public String liste(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page, Model model) {
        Page<Offre> resultat = offres.offresOuvertes(q, PageRequest.of(Math.max(0, page), 10,
                Sort.by(Sort.Direction.DESC, "datePublication")));
        List<OffrePublique> projections = resultat.getContent().stream().map(this::projeter).toList();
        model.addAttribute("offres", resultat);
        model.addAttribute("offresPubliques", projections);
        model.addAttribute("q", q);
        model.addAttribute("titrePage", "Offres d'emploi");
        return "recrutement/vitrine/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Offre offre = offres.trouverOuverte(id);
        model.addAttribute("offre", projeter(offre));
        model.addAttribute("titrePage", offre.getTitre());
        return "recrutement/vitrine/detail";
    }

    private OffrePublique projeter(Offre offre) {
        List<CritereOffrePublic> criteres = new ArrayList<>();
        for (TypeCritere type : TypeCritere.values()) {
            for (CritereOffre c : offres.criteres(offre.getIdOffre(), type)) {
                criteres.add(new CritereOffrePublic(c.getNature().name(), c.getLibelle(), c.getValeurMin(),
                        type == TypeCritere.OBLIGATOIRE));
            }
        }
        return new OffrePublique(offre.getIdOffre(), offre.getReference(), offre.getTitre(),
                offre.getDescription(), offre.getMissions(), offre.getSalairePropose(), offre.getDateLimite(), criteres);
    }
}
