package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.DemandeCongeModel;
import com.gestionrh.sirh.model.DepartementModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.ModeleHoraireModel;
import com.gestionrh.sirh.model.PlanningLigneModel;
import com.gestionrh.sirh.model.PlanningModel;
import com.gestionrh.sirh.repository.AbsenceRepository;
import com.gestionrh.sirh.repository.BudgetDepartementRepository;
import com.gestionrh.sirh.repository.DemandeCongeRepository;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.ModeleHoraireRepository;
import com.gestionrh.sirh.repository.PlanningLigneRepository;
import com.gestionrh.sirh.repository.PlanningRepository;
import com.gestionrh.sirh.util.Formats;

/**
 * Bloc 6 §6 — planning : le manager prépare, le RH valide, la Finance valide s'il y a un impact budgétaire,
 * la DG arbitre un dépassement de budget (RG-PL).
 */
@Service
public class PlanningService {

    private static final Set<String> VERROUILLES = Set.of("CONGE", "ABSENCE");

    /** Case de la grille du planning. */
    public static final class Case {
        private final LocalDate jour;
        private final PlanningLigneModel ligne;
        public Case(LocalDate jour, PlanningLigneModel ligne) {
            this.jour = jour;
            this.ligne = ligne;
        }
        public LocalDate jour() { return jour; }
        public PlanningLigneModel ligne() { return ligne; }

        public String getValeur() {
            return "TRAVAIL".equals(ligne.getTypeJour()) ? String.valueOf(ligne.getModele().getId()) : ligne.getTypeJour();
        }
        public boolean isVerrouillee() { return VERROUILLES.contains(ligne.getTypeJour()); }
        public LocalDate getJour() { return jour; }
        public PlanningLigneModel getLigne() { return ligne; }
    }

    /** Ligne de la grille : un salarié et ses cases jour par jour. */
    public static final class Rangee {
        private final EmployeModel employe;
        private final List<Case> cases;
        private final String totalLibelle;
        public Rangee(EmployeModel employe, List<Case> cases, String totalLibelle) {
            this.employe = employe;
            this.cases = cases;
            this.totalLibelle = totalLibelle;
        }
        public EmployeModel employe() { return employe; }
        public List<Case> cases() { return cases; }
        public String totalLibelle() { return totalLibelle; }

        public EmployeModel getEmploye() { return employe; }
        public List<Case> getCases() { return cases; }
        public String getTotalLibelle() { return totalLibelle; }
    }

    private final PlanningRepository plannings;
    private final PlanningLigneRepository lignes;
    private final DepartementRepository departements;
    private final EmployeRepository employes;
    private final ModeleHoraireRepository modeles;
    private final DemandeCongeRepository conges;
    private final AbsenceRepository absences;
    private final BudgetDepartementRepository budgets;
    private final PrevisionService previsions;
    private final SalaireService salaires;
    private final ManagerService managers;
    private final CalculTempsService calcul;
    private final NotificationService notifications;
    private final TempsParametres parametres;

    public PlanningService(PlanningRepository plannings, PlanningLigneRepository lignes, DepartementRepository departements,
                           EmployeRepository employes, ModeleHoraireRepository modeles, DemandeCongeRepository conges,
                           AbsenceRepository absences, BudgetDepartementRepository budgets, PrevisionService previsions,
                           SalaireService salaires, ManagerService managers, CalculTempsService calcul,
                           NotificationService notifications, TempsParametres parametres) {
        this.plannings = plannings;
        this.lignes = lignes;
        this.departements = departements;
        this.employes = employes;
        this.modeles = modeles;
        this.conges = conges;
        this.absences = absences;
        this.budgets = budgets;
        this.previsions = previsions;
        this.salaires = salaires;
        this.managers = managers;
        this.calcul = calcul;
        this.notifications = notifications;
        this.parametres = parametres;
    }

    // ------------------------------------------------------------ lecture

    /** Départements dont l'utilisateur peut préparer le planning (RG-PL01). */
    @Transactional(readOnly = true)
    public List<DepartementModel> departementsPreparables(EmployeModel u) {
        if ("RH".equals(managers.roleDe(u))) return departements.findAllByOrderByDesignationAsc();
        return managers.equipeDe(u).stream().map(EmployeModel::getDepartement).filter(java.util.Objects::nonNull)
                .collect(Collectors.toMap(DepartementModel::getId, d -> d, (a, b) -> a, LinkedHashMap::new))
                .values().stream().toList();
    }

    @Transactional(readOnly = true)
    public List<PlanningModel> visiblesPar(EmployeModel u) {
        String role = managers.roleDe(u);
        if (Set.of("RH", "FINANCE", "DG").contains(role)) return plannings.findAllByOrderByDateDebutDesc();
        List<Integer> deps = departementsPreparables(u).stream().map(DepartementModel::getId).toList();
        return deps.isEmpty() ? List.of() : plannings.findByDepartement_IdInOrderByDateDebutDesc(deps);
    }

    @Transactional(readOnly = true)
    public List<PlanningModel> aValiderPar(EmployeModel u) {
        return switch (managers.roleDe(u)) {
            case "RH" -> plannings.findByStatutInOrderByDateDebutAsc(List.of("SOUMIS_RH"));
            case "FINANCE" -> plannings.findByStatutInOrderByDateDebutAsc(List.of("SOUMIS_FINANCE"));
            case "DG" -> plannings.findByStatutInOrderByDateDebutAsc(List.of("ARBITRAGE_DG"));
            default -> List.of();
        };
    }

    @Transactional(readOnly = true)
    public PlanningModel charger(Integer id) {
        return plannings.findById(id).orElseThrow(() -> new Bloc4Exception("Planning introuvable."));
    }

    @Transactional(readOnly = true)
    public List<Rangee> grille(Integer id) {
        PlanningModel p = charger(id);
        Map<EmployeModel, List<PlanningLigneModel>> parSalarie = new TreeMap<>(
                Comparator.comparing(EmployeModel::getNom).thenComparing(EmployeModel::getId));
        for (PlanningLigneModel l : lignes.duPlanning(p.getId())) {
            parSalarie.computeIfAbsent(l.getEmploye(), k -> new ArrayList<>()).add(l);
        }
        List<Rangee> rangees = new ArrayList<>();
        parSalarie.forEach((e, ls) -> {
            int total = ls.stream().mapToInt(PlanningLigneModel::getMinutesPrevues).sum();
            rangees.add(new Rangee(e, ls.stream().map(l -> new Case(l.getJour(), l)).toList(),
                    com.gestionrh.sirh.model.JourneeTravailModel.hm(total)));
        });
        return rangees;
    }

    @Transactional(readOnly = true)
    public List<LocalDate> jours(PlanningModel p) {
        return p.getDateDebut().datesUntil(p.getDateFin().plusDays(1)).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal budgetRestant(PlanningModel p) {
        String annee = String.valueOf(p.getDateDebut().getYear());
        BigDecimal alloue = budgets.findAll().stream()
                .filter(b -> p.getDepartement().getId().equals(b.getIdDepartement()) && annee.equals(b.getPeriode()))
                .map(b -> b.getMontantAlloue()).findFirst().orElse(BigDecimal.ZERO);
        return alloue.subtract(plannings.coutEngage(p.getDepartement().getId(), p.getDateDebut().getYear(), p.getId()));
    }

    // ------------------------------------------------------------ préparation (manager)

    @Transactional
    public PlanningModel creer(Integer idDepartement, LocalDate debut, LocalDate fin, EmployeModel auteur) {
        DepartementModel dep = departements.findById(idDepartement)
                .orElseThrow(() -> new Bloc4Exception("Département introuvable."));
        verifierPreparateur(auteur, dep);
        if (debut == null || fin == null || fin.isBefore(debut)) throw new Bloc4Exception("La période est invalide.");
        if (ChronoUnit.DAYS.between(debut, fin) > 31) throw new Bloc4Exception("Un planning couvre au plus 31 jours.");
        if (calcul.periodeCloturee(debut) || calcul.periodeCloturee(fin)) {
            throw new Bloc4Exception("La période est clôturée (RG-PL12).");
        }
        if (!plannings.chevauchants(idDepartement, debut, fin).isEmpty()) {
            throw new Bloc4Exception("Un planning existe déjà pour ce département sur cette période : créez une nouvelle version.");
        }
        List<EmployeModel> equipe = employes.findByDepartement_IdAndActifTrueOrderByNomAsc(idDepartement);
        if (equipe.isEmpty()) throw new Bloc4Exception("Aucun salarié actif dans ce département.");
        PlanningModel p = new PlanningModel();
        p.setReference(reference(debut));
        p.setDepartement(dep);
        p.setDateDebut(debut);
        p.setDateFin(fin);
        p.setAuteur(auteur);
        p = plannings.save(p);
        for (EmployeModel e : equipe) {
            for (LocalDate d = debut; !d.isAfter(fin); d = d.plusDays(1)) {
                PlanningLigneModel l = new PlanningLigneModel();
                l.setPlanning(p);
                l.setEmploye(e);
                l.setJour(d);
                prefiller(l, e, d);
                lignes.save(l);
            }
        }
        return p;
    }

    /** Case pré-remplie : congé ou absence validés (RG-PL04), férié (RG-PL05), sinon le régime. */
    private void prefiller(PlanningLigneModel l, EmployeModel e, LocalDate d) {
        boolean conge = conges.valideesEntre(e.getId(), d, d).stream().anyMatch(c -> joursPleins(c, d));
        if (conge) { appliquer(l, "CONGE", null); return; }
        if (!absences.activesEntre(e.getId(), d, d).isEmpty()) { appliquer(l, "ABSENCE", null); return; }
        PrevisionService.Prevision p = previsions.parDefaut(previsions.regimeAu(e.getId(), d), d);
        appliquer(l, p.typeJour(), p.modele());
    }

    private static boolean joursPleins(DemandeCongeModel c, LocalDate d) {
        return !(d.equals(c.getDateDebut()) && "APRES_MIDI".equals(c.getDemiJourneeDebut()))
                && !(d.equals(c.getDateFin()) && "MATIN".equals(c.getDemiJourneeFin()));
    }

    /** Enregistre la grille saisie : paramètres « c_{idLigne} » = REPOS, FERIE ou id du modèle horaire. */
    @Transactional
    public void enregistrerGrille(Integer id, Map<String, String> valeurs, EmployeModel auteur) {
        PlanningModel p = charger(id);
        verifierPreparateur(auteur, p.getDepartement());
        if (!p.isModifiable()) throw new Bloc4Exception("Seul un planning en brouillon ou rejeté se modifie (RG-PL11).");
        Map<Integer, ModeleHoraireModel> parId = modeles.findAll().stream()
                .collect(Collectors.toMap(ModeleHoraireModel::getId, m -> m));
        for (PlanningLigneModel l : lignes.duPlanning(id)) {
            String v = valeurs.get("c_" + l.getId());
            if (v == null || v.equals(l.getTypeJour()) || VERROUILLES.contains(l.getTypeJour())) continue;
            if ("REPOS".equals(v) || "FERIE".equals(v)) {
                appliquer(l, v, null);
            } else {
                ModeleHoraireModel m = parId.get(Integer.valueOf(v));
                if (m == null || !m.isActif()) throw new Bloc4Exception("Modèle horaire inconnu.");
                appliquer(l, "TRAVAIL", m);
            }
            lignes.save(l);
        }
        p.setMotifRejet(null);
        plannings.save(p);
    }

    /** Calcule durée prévue, heures supplémentaires planifiées et caractère majoré d'une case. */
    private void appliquer(PlanningLigneModel l, String type, ModeleHoraireModel m) {
        l.setTypeJour(type);
        l.setModele("TRAVAIL".equals(type) ? m : null);
        int duree = m == null || !"TRAVAIL".equals(type) ? 0 : m.getDureeMinutes();
        l.setMinutesPrevues(duree);
        var regime = previsions.regimeAu(l.getEmploye().getId(), l.getJour());
        int reference = regime == null ? duree : regime.getMinutesJournalieres();
        l.setMinutesSupPrevues(Math.max(0, duree - reference));
        l.setMajore(duree > 0 && (l.getMinutesSupPrevues() > 0 || m.isEstNuit()
                || l.getJour().getDayOfWeek().getValue() == 7 || previsions.estFerie(l.getJour())));
    }

    /** RG-PL03, RG-PL04, RG-PL10 puis coût (RG-PL06) et passage au RH. */
    @Transactional
    public void soumettre(Integer id, EmployeModel auteur) {
        PlanningModel p = charger(id);
        verifierPreparateur(auteur, p.getDepartement());
        if (!p.isModifiable()) throw new Bloc4Exception("Ce planning a déjà été soumis.");
        List<String> erreurs = controler(p);
        if (!erreurs.isEmpty()) throw new Bloc4Exception(String.join(" ", erreurs));
        evaluerCout(p);
        p.setStatut("SOUMIS_RH");
        p.setDateSoumission(LocalDateTime.now());
        p.setMotifRejet(null);
        plannings.save(p);
        employes.validateursParRole("RH").forEach(rh -> notifications.notifier(rh.getId(), "PLANNING", p.getId(),
                "Planning " + p.getReference() + " (" + p.getDepartement().getDesignation() + ") à valider."));
    }

    public List<String> controler(PlanningModel p) {
        List<String> erreurs = new ArrayList<>();
        LocalDate limite = LocalDate.now().plusDays(parametres.delaiPrevenancePlanningJours);
        if (p.getDateDebut().isBefore(limite)) {
            erreurs.add("RG-PL10 : le planning doit commencer au plus tôt le " + Formats.date(limite) + ".");
        }
        Map<EmployeModel, List<PlanningLigneModel>> parSalarie = lignes.duPlanning(p.getId()).stream()
                .collect(Collectors.groupingBy(PlanningLigneModel::getEmploye, LinkedHashMap::new, Collectors.toList()));
        parSalarie.forEach((e, ls) -> {
            int consecutifs = 0;
            Map<Integer, Integer> parSemaine = new LinkedHashMap<>();
            for (PlanningLigneModel l : ls) {
                boolean travail = "TRAVAIL".equals(l.getTypeJour());
                consecutifs = travail ? consecutifs + 1 : 0;
                if (consecutifs == parametres.joursConsecutifsMax + 1) {
                    erreurs.add("RG-PL03 : " + e.getNomComplet() + " travaille plus de " + parametres.joursConsecutifsMax
                            + " jours d'affilée (repos hebdomadaire).");
                }
                if (l.getMinutesPrevues() > parametres.dureeMaxJourHeures * 60) {
                    erreurs.add("RG-PL03 : " + e.getNomComplet() + " dépasse " + parametres.dureeMaxJourHeures + " h le " + Formats.date(l.getJour()) + ".");
                }
                parSemaine.merge(l.getJour().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR), l.getMinutesPrevues(), Integer::sum);
                if (travail && conges.valideesEntre(e.getId(), l.getJour(), l.getJour()).stream().anyMatch(c -> joursPleins(c, l.getJour()))) {
                    erreurs.add("RG-PL04 : " + e.getNomComplet() + " est en congé validé le " + Formats.date(l.getJour()) + ".");
                }
            }
            parSemaine.forEach((sem, min) -> {
                if (min > parametres.dureeMaxHebdoHeures * 60) {
                    erreurs.add("RG-PL03 : " + e.getNomComplet() + " dépasse " + parametres.dureeMaxHebdoHeures + " h en semaine " + sem + ".");
                }
            });
        });
        return erreurs;
    }

    /** RG-PL06 : coût des heures majorées (heures sup., nuit, dimanche, férié) au taux horaire du salaire en vigueur. */
    private void evaluerCout(PlanningModel p) {
        BigDecimal cout = BigDecimal.ZERO;
        boolean impact = false;
        for (PlanningLigneModel l : lignes.duPlanning(p.getId())) {
            if (!l.isMajore()) continue;
            impact = true;
            BigDecimal taux = salaires.enVigueurAu(l.getEmploye().getId(), l.getJour())
                    .map(h -> h.getSalaireBase().divide(parametres.heuresMensuellesReference, 4, RoundingMode.HALF_UP))
                    .orElse(BigDecimal.ZERO);
            BigDecimal heures = BigDecimal.valueOf(l.getMinutesPrevues()).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            BigDecimal sup = BigDecimal.valueOf(l.getMinutesSupPrevues()).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            cout = cout.add(sup.multiply(taux).multiply(BigDecimal.ONE.add(parametres.majorationHeuresSup)));
            if (l.getModele() != null && l.getModele().isEstNuit()) cout = cout.add(heures.multiply(taux).multiply(parametres.majorationNuit));
            if (l.getJour().getDayOfWeek().getValue() == 7) cout = cout.add(heures.multiply(taux).multiply(parametres.majorationDimanche));
            if (previsions.estFerie(l.getJour())) cout = cout.add(heures.multiply(taux).multiply(parametres.majorationFerie));
        }
        p.setImpactBudgetaire(impact);
        p.setCoutEstime(cout.setScale(2, RoundingMode.HALF_UP));
    }

    // ------------------------------------------------------------ validations (RH → Finance → DG)

    @Transactional
    public void valider(Integer id, EmployeModel u) {
        PlanningModel p = charger(id);
        String role = managers.roleDe(u);
        switch (p.getStatut()) {
            case "SOUMIS_RH" -> {
                exigerRole(role, "RH");
                p.setValideurRh(u);
                p.setDateValidationRh(LocalDateTime.now());
                if (p.isImpactBudgetaire()) {                                        // RG-PL07
                    p.setStatut("SOUMIS_FINANCE");
                    plannings.save(p);
                    employes.validateursParRole("FINANCE").forEach(f -> notifications.notifier(f.getId(), "PLANNING", id,
                            "Planning " + p.getReference() + " : heures majorées estimées à " + p.getCoutFr() + ", validation Finance requise."));
                } else {
                    publier(p);
                }
            }
            case "SOUMIS_FINANCE" -> {
                exigerRole(role, "FINANCE");
                p.setValideurFinance(u);
                p.setDateValidationFinance(LocalDateTime.now());
                if (p.getCoutEstime().compareTo(budgetRestant(p)) > 0) {             // RG-PL08
                    p.setStatut("ARBITRAGE_DG");
                    plannings.save(p);
                    employes.validateursParRole("DG").forEach(d -> notifications.notifier(d.getId(), "PLANNING", id,
                            "Planning " + p.getReference() + " : dépassement du budget du département, arbitrage requis."));
                } else {
                    publier(p);
                }
            }
            case "ARBITRAGE_DG" -> {
                exigerRole(role, "DG");
                p.setDecideurDg(u);
                p.setDateDecisionDg(LocalDateTime.now());
                publier(p);
            }
            default -> throw new Bloc4Exception("Ce planning n'est pas en attente de validation.");
        }
    }

    /** RG-PL09 : tout rejet est motivé et renvoie le planning au manager. */
    @Transactional
    public void rejeter(Integer id, String motif, EmployeModel u) {
        if (motif == null || motif.isBlank()) throw new Bloc4Exception("Le motif du rejet est obligatoire (RG-PL09).");
        PlanningModel p = charger(id);
        String attendu = switch (p.getStatut()) {
            case "SOUMIS_RH" -> "RH";
            case "SOUMIS_FINANCE" -> "FINANCE";
            case "ARBITRAGE_DG" -> "DG";
            default -> throw new Bloc4Exception("Ce planning n'est pas en attente de validation.");
        };
        exigerRole(managers.roleDe(u), attendu);
        p.setStatut("REJETE");
        p.setMotifRejet(motif.trim() + " (" + u.getNomComplet() + ")");
        plannings.save(p);
        notifications.notifier(p.getAuteur().getId(), "PLANNING", id, "Planning " + p.getReference() + " rejeté : " + motif);
    }

    private void publier(PlanningModel p) {
        p.setStatut("PUBLIE");
        p.setDatePublication(LocalDateTime.now());
        plannings.save(p);
        if (p.getIdPlanningPrecedent() != null) {                                   // RG-PL11
            plannings.findById(p.getIdPlanningPrecedent()).ifPresent(ancien -> {
                ancien.setStatut("MODIFIE");
                plannings.save(ancien);
            });
        }
        lignes.duPlanning(p.getId()).stream().map(PlanningLigneModel::getEmploye).distinct().forEach(e -> {
            notifications.notifier(e.getId(), "PLANNING", p.getId(), "Planning du " + p.getPeriodeLibelle() + " publié.");
            calcul.recalculer(e, p.getDateDebut(), p.getDateFin());
        });
        notifications.notifier(p.getAuteur().getId(), "PLANNING", p.getId(), "Planning " + p.getReference() + " publié.");
    }

    /** RG-PL11 : une modification après publication crée une nouvelle version qui repasse le circuit. */
    @Transactional
    public PlanningModel nouvelleVersion(Integer id, EmployeModel auteur) {
        PlanningModel ancien = charger(id);
        verifierPreparateur(auteur, ancien.getDepartement());
        if (!"PUBLIE".equals(ancien.getStatut())) throw new Bloc4Exception("Seul un planning publié peut être révisé.");
        if (calcul.periodeCloturee(ancien.getDateDebut())) throw new Bloc4Exception("La période est clôturée (RG-PL12).");
        PlanningModel p = new PlanningModel();
        p.setReference(reference(ancien.getDateDebut()));
        p.setDepartement(ancien.getDepartement());
        p.setDateDebut(ancien.getDateDebut());
        p.setDateFin(ancien.getDateFin());
        p.setVersion(ancien.getVersion() + 1);
        p.setIdPlanningPrecedent(ancien.getId());
        p.setAuteur(auteur);
        PlanningModel nouveau = plannings.save(p);
        for (PlanningLigneModel l : lignes.duPlanning(id)) {
            PlanningLigneModel c = new PlanningLigneModel();
            c.setPlanning(nouveau);
            c.setEmploye(l.getEmploye());
            c.setJour(l.getJour());
            appliquer(c, l.getTypeJour(), l.getModele());
            lignes.save(c);
        }
        return nouveau;
    }

    /** Bloc 7 → Bloc 6 (RG-PP01, RG-PP02) : un congé validé ou annulé met à jour les plannings non clôturés. */
    @Transactional
    public void synchroniserConge(EmployeModel e, LocalDate debut, LocalDate fin) {
        for (PlanningLigneModel l : lignes.modifiables(e.getId(), debut, fin)) {
            prefiller(l, e, l.getJour());
            lignes.save(l);
        }
    }

    public boolean peutPreparer(EmployeModel u, DepartementModel dep) {
        return "RH".equals(managers.roleDe(u))
                || departementsPreparables(u).stream().anyMatch(d -> d.getId().equals(dep.getId()));
    }

    private void verifierPreparateur(EmployeModel u, DepartementModel dep) {
        if (!peutPreparer(u, dep)) {
            throw new Bloc4Exception("Seul le manager du département (ou le RH) prépare ce planning (RG-PL01).");
        }
    }

    private static void exigerRole(String role, String attendu) {
        if (!attendu.equals(role)) throw new Bloc4Exception("Cette étape de validation revient au profil " + attendu + ".");
    }

    private String reference(LocalDate debut) {
        String prefixe = "PLN-" + debut.getYear() + "-";
        return prefixe + String.format("%04d", plannings.compterReferences(prefixe) + 1);
    }
}
