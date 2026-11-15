package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.GrilleSalarialeModel;
import com.gestionrh.sirh.model.HistoriqueSalaireModel;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.GrilleSalarialeRepository;
import com.gestionrh.sirh.repository.HistoriqueSalaireRepository;
import com.gestionrh.sirh.util.Formats;

/**
 * Bloc 5 §9 — historique de salaire. Une révision n'écrase jamais la ligne précédente :
 * elle la clôt et en ouvre une nouvelle (RG-SA01, RG-SA02).
 */
@Service
public class SalaireService {

    /** Motifs d'une révision saisie par le RH (les autres viennent des contrats). */
    public static final Set<String> MOTIFS_REVISION =
            Set.of("AUGMENTATION_GENERALE", "AUGMENTATION_INDIVIDUELLE", "PROMOTION", "CORRECTION");

    private final HistoriqueSalaireRepository historiques;
    private final EmployeRepository employes;
    private final ContratRepository contrats;
    private final GrilleSalarialeRepository grilles;
    private final CarriereService carriere;
    private final AuditService audit;

    public SalaireService(HistoriqueSalaireRepository historiques, EmployeRepository employes,
                          ContratRepository contrats, GrilleSalarialeRepository grilles,
                          CarriereService carriere, AuditService audit) {
        this.historiques = historiques;
        this.employes = employes;
        this.contrats = contrats;
        this.grilles = grilles;
        this.carriere = carriere;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<HistoriqueSalaireModel> historique(Integer idEmploye) {
        return historiques.historique(idEmploye);
    }

    @Transactional(readOnly = true)
    public Optional<HistoriqueSalaireModel> enCours(Integer idEmploye) {
        return historiques.enCours(idEmploye);
    }

    /** RG-SA01 : salaire dont la période couvre la date donnée (une révision future n'est pas encore en vigueur). */
    @Transactional(readOnly = true)
    public Optional<HistoriqueSalaireModel> enVigueurAu(Integer idEmploye, LocalDate jour) {
        return historiques.historique(idEmploye).stream()
                .filter(h -> !h.getDateEffet().isAfter(jour) && (h.getDateFin() == null || !h.getDateFin().isBefore(jour)))
                .findFirst();
    }

    /** Appelé à l'entrée en vigueur d'un contrat, d'un renouvellement ou d'un avenant (RG-CT09). */
    @Transactional
    public void enregistrerDepuisContrat(ContratModel c, EmployeModel auteur) {
        if (c.getSalaire() == null || c.getSalaire().signum() <= 0) {
            return;
        }
        EmployeModel employe = c.getEmploye();
        String motif = switch (c.getTypeOperation() == null ? "INITIAL" : c.getTypeOperation()) {
            case "AVENANT" -> "AVENANT";
            case "RENOUVELLEMENT" -> "RENOUVELLEMENT";
            default -> "EMBAUCHE";
        };
        LocalDate effet = c.getDateDebut() == null ? LocalDate.now() : c.getDateDebut();
        HistoriqueSalaireModel courant = historiques.enCours(employe.getId()).orElse(null);
        if (courant != null) {
            if (courant.getSalaireBase().compareTo(c.getSalaire()) == 0) {
                return; // même salaire : la ligne en cours reste valable
            }
            if (!effet.isAfter(courant.getDateEffet())) {
                effet = courant.getDateEffet().plusDays(1);
            }
            courant.setDateFin(effet.minusDays(1));
            // Flush : l'index ux_historique_salaire_en_cours n'autorise qu'une ligne ouverte par salarié.
            historiques.saveAndFlush(courant);
        }
        ouvrir(employe, c, c.getSalaire(), c.getCategorieProfessionnelle(), effet, motif,
                "Contrat " + c.getReference(), auteur);
        if (courant != null) {
            carriere.tracer(employe, "REVISION_SALAIRE", effet,
                    "Salaire révisé par le contrat " + c.getReference(),
                    Formats.montant(courant.getSalaireBase()), Formats.montant(c.getSalaire()),
                    "CONTRAT", c.getId(), auteur);
        }
    }

    /** Révision salariale saisie par le RH (Bloc 5 §9.3). */
    @Transactional
    public HistoriqueSalaireModel reviser(Integer idEmploye, BigDecimal nouveau, LocalDate dateEffet,
                                         String motif, String commentaire, EmployeModel auteur) {
        EmployeModel employe = employes.findById(idEmploye)
                .orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        if ("SORTI".equals(employe.getStatutAdministratif())) {
            throw new Bloc4Exception("Le dossier d'un salarié sorti est en lecture seule.");
        }
        if (motif == null || !MOTIFS_REVISION.contains(motif)) {
            throw new Bloc4Exception("Le motif de la révision est invalide.");
        }
        if (nouveau == null || nouveau.signum() <= 0) {
            throw new Bloc4Exception("Le nouveau salaire doit être supérieur à zéro.");
        }
        if (dateEffet == null) {
            throw new Bloc4Exception("La date d'effet est obligatoire.");
        }
        if ("CORRECTION".equals(motif) && (commentaire == null || commentaire.isBlank())) {
            throw new Bloc4Exception("Une correction doit être justifiée par un commentaire.");
        }
        HistoriqueSalaireModel courant = historiques.enCours(idEmploye)
                .orElseThrow(() -> new Bloc4Exception(
                        "Aucun salaire en vigueur : le salaire initial provient du contrat signé et activé."));
        int comparaison = nouveau.compareTo(courant.getSalaireBase());
        if (comparaison == 0) {
            throw new Bloc4Exception("Le nouveau salaire est identique au salaire en vigueur.");
        }
        if (comparaison < 0 && !"CORRECTION".equals(motif)) {
            throw new Bloc4Exception("RG-SA04 : une baisse de salaire exige un avenant signé par le salarié.");
        }
        if (!dateEffet.isAfter(courant.getDateEffet())) {
            throw new Bloc4Exception("La date d'effet doit être postérieure au "
                    + Formats.date(courant.getDateEffet()) + " (début du salaire en vigueur).");
        }
        ContratModel contrat = contrats.findByIdEmployeAndStatutWorkflowInOrderByDateDebutDesc(
                idEmploye, List.of("EN_VIGUEUR")).stream().findFirst().orElse(null);
        BigDecimal minimum = minimumGrille(employe, contrat);
        if (minimum != null && nouveau.compareTo(minimum) < 0) {
            throw new Bloc4Exception("RG-SA03 : le salaire est inférieur au minimum de la grille ("
                    + Formats.montant(minimum) + ").");
        }

        courant.setDateFin(dateEffet.minusDays(1));
        historiques.saveAndFlush(courant);
        HistoriqueSalaireModel h = ouvrir(employe, contrat, nouveau,
                contrat == null ? courant.getCategorieProfessionnelle() : contrat.getCategorieProfessionnelle(),
                dateEffet, motif, commentaire, auteur);

        boolean retroactif = dateEffet.isBefore(LocalDate.now());
        carriere.tracer(employe, "PROMOTION".equals(motif) ? "PROMOTION" : "REVISION_SALAIRE", dateEffet,
                h.getMotifLibelle() + (retroactif ? " (rétroactive : rappel à calculer en paie)" : ""),
                Formats.montant(courant.getSalaireBase()), Formats.montant(nouveau),
                "SALAIRE", h.getId(), auteur);
        audit.tracer("SALARIE", idEmploye, "REVISION_SALAIRE", Formats.montant(courant.getSalaireBase()),
                Formats.montant(nouveau), commentaire, auteur);
        return h;
    }

    private HistoriqueSalaireModel ouvrir(EmployeModel employe, ContratModel contrat, BigDecimal salaire,
                                          String categorie, LocalDate effet, String motif,
                                          String commentaire, EmployeModel auteur) {
        HistoriqueSalaireModel h = new HistoriqueSalaireModel();
        h.setEmploye(employe);
        h.setContrat(contrat);
        h.setSalaireBase(salaire);
        h.setCategorieProfessionnelle(categorie);
        h.setDateEffet(effet);
        h.setMotif(motif);
        h.setCommentaire(commentaire);
        h.setValidePar(auteur);
        return historiques.save(h);
    }

    private BigDecimal minimumGrille(EmployeModel employe, ContratModel contrat) {
        if (employe.getPoste() == null || contrat == null || contrat.getTypeContrat() == null) {
            return null;
        }
        return grilles.candidats(employe.getPoste().getId(),
                        employe.getDepartement() == null ? null : employe.getDepartement().getId(),
                        contrat.getTypeContrat().getId()).stream()
                .map(GrilleSalarialeModel::getSalaire)
                .filter(java.util.Objects::nonNull)
                .min(BigDecimal::compareTo).orElse(null);
    }
}
