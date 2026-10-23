package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.dto.recrutement.CritereForm;
import com.gestionrh.sirh.dto.recrutement.OffreForm;
import com.gestionrh.sirh.enums.recrutement.OffreStatut;
import com.gestionrh.sirh.enums.recrutement.TypeCritere;
import com.gestionrh.sirh.event.recrutement.OffreAnnuleeEvent;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.exception.recrutement.RessourceIntrouvableException;
import com.gestionrh.sirh.model.recrutement.CritereOffre;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.CritereOffreRepository;
import com.gestionrh.sirh.repository.recrutement.OffreRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regles des offres d'emploi (bloc 2A, etape 1).
 * Cycle : BROUILLON -> VALIDEE -> PUBLIEE <-> SUSPENDUE -> TERMINEE ; ANNULEE possible avant la fin.
 */
@Service
@Transactional(readOnly = true)
public class OffreService {

    /** Une demande ne donne pas plusieurs offres actives : ces statuts ne comptent plus. */
    private static final List<OffreStatut> STATUTS_FERMES = List.of(OffreStatut.ANNULEE, OffreStatut.TERMINEE);

    private final OffreRepository offres;
    private final CritereOffreRepository criteres;
    private final CandidatureRepository candidatures;
    private final ApplicationEventPublisher events;

    public OffreService(OffreRepository offres, CritereOffreRepository criteres, CandidatureRepository candidatures,
            ApplicationEventPublisher events) {
        this.offres = offres;
        this.criteres = criteres;
        this.candidatures = candidatures;
        this.events = events;
    }

    // ------------------------------------------------------------ lecture

    public Page<Offre> rechercher(String q, OffreStatut statut, Pageable pageable) {
        String titre = q == null ? "" : q.trim();
        if (statut == null) {
            return titre.isEmpty() ? offres.findAll(pageable) : offres.findByTitreContainingIgnoreCase(titre, pageable);
        }
        return titre.isEmpty()
                ? offres.findByStatut(statut, pageable)
                : offres.findByStatutAndTitreContainingIgnoreCase(statut, titre, pageable);
    }

    public Offre trouver(Integer id) {
        return offres.findById(id).orElseThrow(() -> new RessourceIntrouvableException("Offre introuvable : " + id));
    }

    /** Règle unique d'ouverture : publiée et date limite égale ou postérieure à aujourd'hui. */
    public boolean estOuverte(Offre offre) {
        return offre != null && offre.getStatut() == OffreStatut.PUBLIEE
                && offre.getDateLimite() != null && !offre.getDateLimite().isBefore(LocalDate.now());
    }

    public Offre trouverOuverte(Integer id) {
        Offre offre = trouver(id);
        if (!estOuverte(offre)) throw new RegleMetierException("Cette offre est fermée et n'accepte plus de candidatures.");
        return offre;
    }

    public Page<Offre> offresOuvertes(String q, Pageable pageable) {
        String titre = q == null ? "" : q.trim();
        LocalDate aujourdHui = LocalDate.now();
        return titre.isEmpty()
                ? offres.findByStatutAndDateLimiteGreaterThanEqual(OffreStatut.PUBLIEE, aujourdHui, pageable)
                : offres.findByStatutAndDateLimiteGreaterThanEqualAndTitreContainingIgnoreCase(
                        OffreStatut.PUBLIEE, aujourdHui, titre, pageable);
    }

    @Transactional
    public void recalculerNombreCandidatures(Integer idOffre) {
        Offre offre = trouver(idOffre);
        offre.setNbCandidatures(Math.toIntExact(candidatures.countByOffre_IdOffreAndStatutNot(
                idOffre, com.gestionrh.sirh.enums.recrutement.CandidatureStatut.RETIREE)));
    }

    public List<CritereOffre> criteres(Integer idOffre, TypeCritere type) {
        return criteres.findByOffre_IdOffreAndTypeCritere(idOffre, type);
    }

    /** Liste des informations qui empechent la validation / la publication (vide = offre complete). */
    public List<String> champsManquants(Offre o) {
        List<String> m = new ArrayList<>();
        if (vide(o.getTitre())) m.add("Titre");
        if (vide(o.getDescription())) m.add("Description");
        if (vide(o.getMissions())) m.add("Missions");
        if (o.getSalairePropose() == null) m.add("Salaire propose");
        if (o.getNombrePostes() == null || o.getNombrePostes() < 1) m.add("Nombre de postes (au moins 1)");
        if (o.getDateLimite() == null) {
            m.add("Date limite");
        } else if (!o.getDateLimite().isAfter(LocalDate.now())) {
            m.add("Date limite (doit etre dans le futur)");
        }
        if (criteres.findByOffre_IdOffreAndTypeCritere(o.getIdOffre(), TypeCritere.OBLIGATOIRE).isEmpty()) {
            m.add("Au moins un critere obligatoire");
        }
        return m;
    }

    // ------------------------------------------------------------ CRUD

    @Transactional
    Offre creer(OffreForm f) {
        if (offres.existsByIdDemandeBesoinAndStatutNotIn(f.getIdDemandeBesoin(), STATUTS_FERMES)) {
            throw new RegleMetierException("Cette demande a deja une offre en cours.");
        }
        Offre o = new Offre();
        o.setReference("TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        o.setIdDemandeBesoin(f.getIdDemandeBesoin());
        o.setIdPoste(f.getIdPoste());
        o.setIdDepartement(f.getIdDepartement());
        o.setIdTypeContrat(f.getIdTypeContrat());
        appliquer(o, f);
        o = offres.saveAndFlush(o);
        // la reference definitive utilise l'identifiant genere : OFF-2026-0001
        o.setReference("OFF-%d-%04d".formatted(Year.now().getValue(), o.getIdOffre()));
        return o;
    }

    @Transactional
    public Offre modifier(Integer id, OffreForm f) {
        Offre o = trouver(id);
        exiger(o, "modifier", OffreStatut.BROUILLON);
        appliquer(o, f);
        return o;
    }

    /** Suppression simple : uniquement un brouillon. Une offre publiee suit la procedure d'annulation. */
    @Transactional
    public void supprimer(Integer id) {
        Offre o = trouver(id);
        if (o.getStatut() != OffreStatut.BROUILLON) {
            throw new RegleMetierException("Seul un brouillon peut etre supprime. Utilisez l'annulation pour une offre deja validee ou publiee.");
        }
        if (candidatures.existsByOffre_IdOffre(id)) {
            throw new RegleMetierException("Cette offre a deja des candidatures : suppression impossible.");
        }
        offres.delete(o);
    }

    // ------------------------------------------------------------ cycle de vie

    @Transactional
    public Offre valider(Integer id, Integer idValidateur) {
        Offre o = trouver(id);
        exiger(o, "valider", OffreStatut.BROUILLON);
        verifierComplete(o);
        o.setStatut(OffreStatut.VALIDEE);
        o.setIdValidateur(idValidateur);
        o.setDateValidation(LocalDateTime.now());
        return o;
    }

    @Transactional
    public Offre publier(Integer id) {
        Offre o = trouver(id);
        exiger(o, "publier", OffreStatut.VALIDEE);
        verifierComplete(o);
        o.setStatut(OffreStatut.PUBLIEE);
        o.setDatePublication(LocalDateTime.now());
        return o;
    }

    @Transactional
    public Offre suspendre(Integer id) {
        Offre o = trouver(id);
        exiger(o, "suspendre", OffreStatut.PUBLIEE);
        o.setStatut(OffreStatut.SUSPENDUE);
        return o;
    }

    @Transactional
    public Offre reprendre(Integer id) {
        Offre o = trouver(id);
        exiger(o, "reprendre", OffreStatut.SUSPENDUE);
        if (o.getDateLimite() == null || !o.getDateLimite().isAfter(LocalDate.now())) {
            throw new RegleMetierException("La date limite est depassee : l'offre ne peut plus etre reprise.");
        }
        o.setStatut(OffreStatut.PUBLIEE);
        return o;
    }

    @Transactional
    public Offre terminer(Integer id) {
        Offre o = trouver(id);
        exiger(o, "terminer", OffreStatut.PUBLIEE, OffreStatut.SUSPENDUE);
        o.setStatut(OffreStatut.TERMINEE);
        return o;
    }

    @Transactional
    public Offre annuler(Integer id) {
        Offre o = trouver(id);
        exiger(o, "annuler", OffreStatut.BROUILLON, OffreStatut.VALIDEE, OffreStatut.PUBLIEE, OffreStatut.SUSPENDUE);
        o.setStatut(OffreStatut.ANNULEE);
        events.publishEvent(new OffreAnnuleeEvent(id));
        return o;
    }

    // ------------------------------------------------------------ criteres

    @Transactional
    public CritereOffre ajouterCritere(Integer idOffre, CritereForm f) {
        Offre o = trouver(idOffre);
        exiger(o, "modifier les criteres de", OffreStatut.BROUILLON);
        if (f.getNature() == null) throw new RegleMetierException("Choisissez la nature du critere.");

        CritereOffre c = new CritereOffre();
        c.setTypeCritere(f.getTypeCritere() == null ? TypeCritere.OBLIGATOIRE : f.getTypeCritere());
        c.setNature(f.getNature());
        switch (f.getNature()) {
            case NIVEAU_ETUDE -> {
                if (f.getNiveauEtude() == null) throw new RegleMetierException("Choisissez le niveau d'etude.");
                c.setLibelle(f.getNiveauEtude().name());
            }
            case EXPERIENCE -> {
                if (f.getValeurMin() == null || f.getValeurMin() < 0) {
                    throw new RegleMetierException("Indiquez le nombre d'annees d'experience (0 ou plus).");
                }
                c.setValeurMin(f.getValeurMin());
            }
            case COMPETENCE, LANGUE -> {
                if (vide(f.getLibelle())) throw new RegleMetierException("Indiquez la competence ou la langue.");
                c.setLibelle(f.getLibelle().trim());
            }
        }
        boolean doublon = criteres.findByOffre_IdOffre(idOffre).stream().anyMatch(e ->
                e.getTypeCritere() == c.getTypeCritere() && e.getNature() == c.getNature()
                        && Objects.equals(e.getLibelle() == null ? null : e.getLibelle().toLowerCase(),
                                          c.getLibelle() == null ? null : c.getLibelle().toLowerCase()));
        if (doublon) throw new RegleMetierException("Ce critere existe deja pour cette offre.");

        c.setOffre(o);
        return criteres.save(c);
    }

    @Transactional
    public void supprimerCritere(Integer idOffre, Integer idCritere) {
        Offre o = trouver(idOffre);
        exiger(o, "modifier les criteres de", OffreStatut.BROUILLON);
        CritereOffre c = criteres.findById(idCritere)
                .orElseThrow(() -> new RessourceIntrouvableException("Critere introuvable : " + idCritere));
        if (!c.getOffre().getIdOffre().equals(idOffre)) {
            throw new RessourceIntrouvableException("Ce critere n'appartient pas a cette offre.");
        }
        criteres.delete(c);
    }

    // ------------------------------------------------------------ outils

    private void appliquer(Offre o, OffreForm f) {
        o.setTitre(f.getTitre().trim());
        o.setDescription(f.getDescription());
        o.setMissions(f.getMissions());
        o.setSalairePropose(f.getSalairePropose());
        o.setNombrePostes(f.getNombrePostes());
        o.setDureeMois(f.getDureeMois());
        o.setDateLimite(f.getDateLimite());
    }

    private void exiger(Offre o, String action, OffreStatut... autorises) {
        if (!Arrays.asList(autorises).contains(o.getStatut())) {
            throw new RegleMetierException("Impossible de " + action + " une offre au statut " + o.getStatut() + ".");
        }
    }

    private void verifierComplete(Offre o) {
        List<String> manquants = champsManquants(o);
        if (!manquants.isEmpty()) {
            throw new RegleMetierException("Offre incomplete. A renseigner : " + String.join(", ", manquants) + ".");
        }
    }

    private static boolean vide(String s) {
        return s == null || s.isBlank();
    }
}
