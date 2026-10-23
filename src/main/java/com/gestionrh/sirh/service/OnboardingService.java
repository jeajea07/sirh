package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.ModeleOnboardingModel;
import com.gestionrh.sirh.model.ModeleTacheOnboardingModel;
import com.gestionrh.sirh.model.OnboardingModel;
import com.gestionrh.sirh.model.OnboardingTacheModel;
import com.gestionrh.sirh.model.UtilisateurModel;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.ModeleOnboardingRepository;
import com.gestionrh.sirh.repository.OnboardingRepository;
import com.gestionrh.sirh.repository.OnboardingTacheRepository;
import com.gestionrh.sirh.repository.UtilisateurRepository;
import com.gestionrh.sirh.service.dto.EvenementAuditVue;
import com.gestionrh.sirh.service.dto.OnboardingDetail;
import com.gestionrh.sirh.service.dto.OnboardingResume;
import com.gestionrh.sirh.service.dto.OnboardingVue;
import com.gestionrh.sirh.service.dto.TacheVue;
import com.gestionrh.sirh.util.Formats;

@Service
public class OnboardingService {

    private static final List<String> STATUTS_ACTIFS = List.of("A_FAIRE", "EN_COURS", "BLOQUEE");

    private final OnboardingRepository onboardings;
    private final OnboardingTacheRepository taches;
    private final ModeleOnboardingRepository modeles;
    private final ContratRepository contrats;
    private final EmployeRepository employes;
    private final UtilisateurRepository utilisateurs;
    private final AuditService audit;
    private final NotificationService notifications;

    public OnboardingService(OnboardingRepository onboardings, OnboardingTacheRepository taches,
                             ModeleOnboardingRepository modeles, ContratRepository contrats,
                             EmployeRepository employes, UtilisateurRepository utilisateurs,
                             AuditService audit, NotificationService notifications) {
        this.onboardings = onboardings;
        this.taches = taches;
        this.modeles = modeles;
        this.contrats = contrats;
        this.employes = employes;
        this.utilisateurs = utilisateurs;
        this.audit = audit;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public Page<OnboardingResume> lister(String statut, int page, int taille) {
        int p = Math.max(page, 0);
        int t = Math.min(Math.max(taille, 1), 50);
        PageRequest pageable = PageRequest.of(p, t, Sort.by("dateCreation").descending());
        Page<OnboardingModel> source = (statut == null || statut.isBlank())
                ? onboardings.findAll(pageable) : onboardings.findByStatut(statut, pageable);
        List<Integer> ids = source.getContent().stream().map(OnboardingModel::getId).toList();
        Map<Integer, List<OnboardingTacheModel>> parOnboarding = new HashMap<>();
        if (!ids.isEmpty()) {
            for (OnboardingTacheModel tache : taches.findByIdOnboardingIn(ids)) {
                parOnboarding.computeIfAbsent(tache.getOnboarding().getId(), k -> new ArrayList<>()).add(tache);
            }
        }
        return source.map(o -> resume(o, parOnboarding.getOrDefault(o.getId(), List.of())));
    }

    @Transactional(readOnly = true)
    public OnboardingDetail detail(Integer id) {
        OnboardingModel o = charger(id);
        OnboardingDetail d = new OnboardingDetail();
        d.setOnboarding(vue(o));
        List<TacheVue> vues = o.getTaches().stream().map(this::tacheVue).toList();
        d.setTaches(vues);
        d.setTachesTerminees((int) vues.stream().filter(TacheVue::isTerminee).count());
        d.setTachesEnRetard((int) vues.stream().filter(TacheVue::isEnRetard).count());
        d.setTachesBloquees((int) vues.stream().filter(TacheVue::isBloquee).count());
        d.setTachesRestantes((int) vues.stream().filter(t -> !t.isTerminee() && !t.isAnnulee()).count());
        List<String> raisons = new ArrayList<>();
        o.getTaches().stream()
                .filter(t -> t.isObligatoire() && !t.isTerminee())
                .forEach(t -> raisons.add("Tâche obligatoire non terminée : " + t.getTitre()));
        o.getTaches().stream()
                .filter(t -> t.isBloquante() && t.isBloquee())
                .forEach(t -> raisons.add("Tâche bloquante bloquée : " + t.getTitre()));
        d.setRaisonsNonCloture(raisons);
        d.setPeutCloturer(raisons.isEmpty() && !o.isCloture() && !o.isAnnule());
        d.setHistorique(historique(id));
        return d;
    }

    @Transactional(readOnly = true)
    public List<OnboardingResume> listerParEmploye(Integer idEmploye) {
        return onboardings.findByIdEmployeOrderByDateCreationDesc(idEmploye).stream()
                .map(o -> resume(o, o.getTaches())).toList();
    }

    @Transactional(readOnly = true)
    public List<OnboardingResume> listerParContrat(Integer idContrat) {
        return onboardings.findByContrat_IdOrderByDateCreationDesc(idContrat).stream()
                .map(o -> resume(o, o.getTaches())).toList();
    }

    @Transactional
    public OnboardingModel creer(Integer idEmploye, Integer idContrat,
                                 Integer idModele, LocalDate dateDebutPrevue,
                                 EmployeModel auteur) {
        EmployeModel employe = employes.findById(idEmploye)
                .orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        ContratModel contrat = contrats.findById(idContrat)
                .orElseThrow(() -> new Bloc4Exception("Contrat introuvable."));
        ModeleOnboardingModel modele = modeles.findAvecTachesById(idModele)
                .orElseThrow(() -> new Bloc4Exception("Modèle d'onboarding introuvable."));
        if (!modele.isActif()) {
            throw new Bloc4Exception("Le modèle d'onboarding doit être actif.");
        }
        EmployeModel rh = auteur;
        if (rh == null || !estRh(rh)) {
            rh = employes.validateursParRole("RH").stream().findFirst()
                    .orElseThrow(() -> new Bloc4Exception("Aucun responsable RH n'est disponible."));
        }
        LocalDate debut = dateDebutPrevue != null ? dateDebutPrevue : contrat.getDateDebut();

        OnboardingModel o = new OnboardingModel();
        o.setReference(genererReference());
        o.setEmploye(employe);
        o.setContrat(contrat);
        o.setModele(modele);
        o.setVersionModele(modele.getVersion());
        o.setDateDebutPrevue(debut);
        o.setDateFinPrevue(debut.plusDays(Math.max(modele.getDureeIndicativeJours(), 30)));
        o.setStatut("A_PREPARER");
        o.setResponsableRh(rh);
        o.setDateCreation(LocalDateTime.now());
        o.setDateMaj(LocalDateTime.now());
        o.setCreePar(auteur);

        for (ModeleTacheOnboardingModel mt : modele.getTaches()) {
            if (!conditionRemplie(mt.getConditionActivation(), employe, contrat)) {
                continue;
            }
            OnboardingTacheModel t = new OnboardingTacheModel();
            t.setOnboarding(o);
            t.setModeleTache(mt);
            t.setTitre(mt.getTitre());
            t.setDescription(mt.getDescription());
            t.setRoleResponsable(mt.getRoleResponsable());
            t.setDateEcheance(debut.plusDays(mt.getDelaiIndicatifJours() == null ? 0 : mt.getDelaiIndicatifJours()));
            t.setStatut("A_FAIRE");
            t.setObligatoire(mt.isObligatoire());
            t.setBloquante(mt.isBloquante());
            t.setOrdre(mt.getOrdre());
            t.setResponsable(responsablePour(mt.getRoleResponsable(), employe, rh));
            t.setDateCreation(LocalDateTime.now());
            t.setDateMaj(LocalDateTime.now());
            o.getTaches().add(t);
        }
        o = onboardings.save(o);
        for (OnboardingTacheModel t : o.getTaches()) {
            if (t.getModeleTache() != null && t.getModeleTache().getDependDeTache() != null) {
                Integer idDependance = t.getModeleTache().getDependDeTache().getId();
                o.getTaches().stream()
                        .filter(c -> c.getModeleTache() != null && idDependance.equals(c.getModeleTache().getId()))
                        .findFirst().ifPresent(t::setDependDeTache);
            }
        }
        o = onboardings.save(o);
        audit.tracer("ONBOARDING", o.getId(), "CREATION", null, o.getReference(), null, auteur);
        notifierRh("ONBOARDING", o.getId(), "Onboarding " + o.getReference() + " créé.");
        return o;
    }

    @Transactional
    public Optional<OnboardingModel> creerAutomatique(Integer idContrat, EmployeModel auteur) {
        ContratModel contrat = contrats.findById(idContrat)
                .orElseThrow(() -> new Bloc4Exception("Contrat introuvable."));
        Optional<OnboardingModel> existant = onboardings.findFirstByContrat_IdOrderByDateCreationDesc(idContrat);
        if (existant.isPresent()) {
            return existant;
        }
        EmployeModel employe = contrat.getEmploye();
        Integer idPoste = employe.getPoste() == null ? null : employe.getPoste().getId();
        Integer idDepartement = employe.getDepartement() == null ? null : employe.getDepartement().getId();
        List<ModeleOnboardingModel> candidats = modeles.candidatsPour(idPoste, idDepartement);
        if (candidats.isEmpty()) {
            return Optional.empty();
        }
        candidats.sort(Comparator
                .comparingInt((ModeleOnboardingModel m) -> specificite(m, idPoste, idDepartement)).reversed()
                .thenComparing(Comparator.comparingInt(
                        (ModeleOnboardingModel m) -> m.getVersion() == null ? 0 : m.getVersion()).reversed()));
        return Optional.of(creer(employe.getId(), idContrat, candidats.get(0).getId(),
                contrat.getDateDebut(), auteur));
    }

    @Transactional
    public void demarrerTache(Integer idTache, EmployeModel auteur) {
        OnboardingTacheModel t = chargerTache(idTache);
        verifierResponsable(t, auteur);
        if (t.isTerminee() || "ANNULEE".equals(t.getStatut())) {
            throw new Bloc4Exception("Cette tâche est déjà terminée ou annulée.");
        }
        if (!t.isDependancePrete()) {
            throw new Bloc4Exception("La tâche dont dépend cette étape n'est pas terminée.");
        }
        t.setStatut("EN_COURS");
        if (t.getDateDebut() == null) t.setDateDebut(LocalDateTime.now());
        t.setDateMaj(LocalDateTime.now());
        taches.save(t);
        if (t.getOnboarding().getDateDebutReelle() == null) {
            t.getOnboarding().setDateDebutReelle(LocalDate.now());
            onboardings.save(t.getOnboarding());
        }
        recalculerStatut(t.getOnboarding().getId());
        audit.tracer("TACHE", idTache, "DEMARRAGE", "A_FAIRE", "EN_COURS", null, auteur);
    }

    @Transactional
    public void terminerTache(Integer idTache, String commentaire, EmployeModel auteur) {
        OnboardingTacheModel t = chargerTache(idTache);
        verifierResponsable(t, auteur);
        if (!t.isDependancePrete()) {
            throw new Bloc4Exception("La tâche dont dépend cette étape n'est pas terminée.");
        }
        if (t.isTerminee()) {
            throw new Bloc4Exception("Cette tâche est déjà terminée.");
        }
        t.setStatut("TERMINEE");
        t.setDateRealisation(LocalDateTime.now());
        t.setRealisePar(auteur);
        t.setCommentaire(commentaire);
        t.setDateMaj(LocalDateTime.now());
        taches.save(t);
        recalculerStatut(t.getOnboarding().getId());
        audit.tracer("TACHE", idTache, "TERMINAISON", null, "TERMINEE", commentaire, auteur);
    }

    @Transactional
    public void bloquerTache(Integer idTache, String motif, EmployeModel auteur) {
        if (motif == null || motif.isBlank()) {
            throw new Bloc4Exception("Le motif de blocage est obligatoire.");
        }
        OnboardingTacheModel t = chargerTache(idTache);
        verifierResponsable(t, auteur);
        t.setStatut("BLOQUEE");
        t.setMotifBlocage(motif);
        t.setDateMaj(LocalDateTime.now());
        taches.save(t);
        if (t.isBloquante()) {
            notifierRh("TACHE", idTache, "Tâche bloquante : " + t.getTitre() + " — " + motif);
        }
        recalculerStatut(t.getOnboarding().getId());
        audit.tracer("TACHE", idTache, "BLOCAGE", null, "BLOQUEE", motif, auteur);
    }

    @Transactional
    public void debloquerTache(Integer idTache, EmployeModel auteur) {
        OnboardingTacheModel t = chargerTache(idTache);
        verifierResponsable(t, auteur);
        if (!"BLOQUEE".equals(t.getStatut())) {
            throw new Bloc4Exception("Seule une tâche bloquée peut être débloquée.");
        }
        String avant = t.getStatut();
        t.setStatut(t.getDateDebut() == null ? "A_FAIRE" : "EN_COURS");
        t.setMotifBlocage(null);
        t.setDateMaj(LocalDateTime.now());
        taches.save(t);
        recalculerStatut(t.getOnboarding().getId());
        audit.tracer("TACHE", idTache, "DEBLOCAGE", avant, t.getStatut(), null, auteur);
    }

    @Transactional
    public void reassignerTache(Integer idTache, Integer idResponsable, EmployeModel auteur) {
        if (!estRh(auteur)) {
            throw new Bloc4Exception("Seule la RH peut réassigner une tâche.");
        }
        OnboardingTacheModel t = chargerTache(idTache);
        EmployeModel nouveau = employes.findById(idResponsable)
                .filter(EmployeModel::isActif)
                .orElseThrow(() -> new Bloc4Exception("Le nouveau responsable doit être un salarié actif."));
        EmployeModel ancien = t.getResponsable();
        t.setResponsable(nouveau);
        t.setDateMaj(LocalDateTime.now());
        taches.save(t);
        notifications.notifier(nouveau.getId(), "TACHE", idTache,
                "Une tâche vous a été assignée : " + t.getTitre());
        audit.tracer("TACHE", idTache, "REASSIGNATION",
                ancien == null ? null : ancien.getNomComplet(), nouveau.getNomComplet(), null, auteur);
    }

    @Transactional
    public void cloturer(Integer id, EmployeModel auteur) {
        OnboardingModel o = charger(id);
        if (o.isCloture()) {
            throw new Bloc4Exception("Cet onboarding est déjà clôturé.");
        }
        if (!o.isCloturable()) {
            throw new Bloc4Exception("Toutes les tâches obligatoires doivent être terminées avant la clôture.");
        }
        if (o.getTaches().stream().anyMatch(t -> t.isBloquante() && t.isBloquee())) {
            throw new Bloc4Exception("Une tâche bloquante est encore bloquée.");
        }
        o.setStatut("CLOTURE");
        o.setDateCloture(LocalDateTime.now());
        o.setCloturePar(auteur);
        o.setDateMaj(LocalDateTime.now());
        onboardings.save(o);
        notifierRh("ONBOARDING", id, "Onboarding " + o.getReference() + " clôturé.");
        audit.tracer("ONBOARDING", id, "CLOTURE", null, "CLOTURE", null, auteur);
    }

    @Transactional
    public void annuler(Integer id, String motif, EmployeModel auteur) {
        if (motif == null || motif.isBlank()) {
            throw new Bloc4Exception("Le motif d'annulation est obligatoire.");
        }
        OnboardingModel o = charger(id);
        if (o.isCloture()) {
            throw new Bloc4Exception("Un onboarding clôturé doit d'abord être rouvert.");
        }
        o.setStatut("ANNULE");
        o.setMotifAnnulation(motif);
        o.setDateMaj(LocalDateTime.now());
        o.getTaches().stream().filter(t -> !t.isTerminee()).forEach(t -> t.setStatut("ANNULEE"));
        onboardings.save(o);
        audit.tracer("ONBOARDING", id, "ANNULATION", null, "ANNULE", motif, auteur);
    }

    @Transactional
    public void rouvrir(Integer id, String motif, EmployeModel auteur) {
        if (motif == null || motif.isBlank()) {
            throw new Bloc4Exception("Le motif de réouverture est obligatoire.");
        }
        OnboardingModel o = charger(id);
        if (!o.isCloture()) {
            throw new Bloc4Exception("Seul un onboarding clôturé peut être rouvert.");
        }
        o.setStatut("EN_COURS");
        o.setDateCloture(null);
        o.setCloturePar(null);
        o.setDateMaj(LocalDateTime.now());
        onboardings.save(o);
        audit.tracer("ONBOARDING", id, "REOUVERTURE", "CLOTURE", "EN_COURS", motif, auteur);
    }

    @Transactional
    public void recalculerStatut(Integer id) {
        OnboardingModel o = charger(id);
        if (o.isCloture() || o.isAnnule()) return;
        if (o.getTaches().stream().anyMatch(t -> t.isBloquante() && t.isBloquee())) {
            o.setStatut("BLOQUE");
        } else if (!o.getTaches().isEmpty() && o.getTaches().stream()
                .filter(OnboardingTacheModel::isObligatoire).allMatch(OnboardingTacheModel::isTerminee)) {
            o.setStatut("A_VERIFIER");
        } else if (o.getTaches().stream().anyMatch(t -> "EN_COURS".equals(t.getStatut()) || t.isTerminee())) {
            o.setStatut("EN_COURS");
        } else if (o.getDateDebutReelle() != null) {
            o.setStatut("PRET");
        } else {
            o.setStatut("A_PREPARER");
        }
        o.setDateMaj(LocalDateTime.now());
        onboardings.save(o);
    }

    @Transactional(readOnly = true)
    public int progression(Integer id) {
        OnboardingModel o = charger(id);
        return o.getProgression();
    }

    @Transactional(readOnly = true)
    public List<TacheVue> tachesDe(Integer idEmploye) {
        return taches.findByIdResponsableAndStatutInOrderByDateEcheanceAsc(idEmploye, STATUTS_ACTIFS)
                .stream().map(this::tacheVue).toList();
    }

    @Transactional(readOnly = true)
    public List<TacheVue> tachesEnRetard() {
        return taches.enRetard(LocalDate.now()).stream().map(this::tacheVue).toList();
    }

    @Transactional(readOnly = true)
    public boolean tacheAppartient(Integer idTache, Integer idOnboarding) {
        return taches.findById(idTache)
                .map(t -> t.getOnboarding().getId().equals(idOnboarding))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public int compterTachesDe(Integer idEmploye) {
        return (int) taches.countByIdResponsableAndStatutIn(idEmploye, STATUTS_ACTIFS);
    }

    private OnboardingModel charger(Integer id) {
        return onboardings.findAvecRelationsById(id)
                .orElseThrow(() -> new Bloc4Exception("Onboarding introuvable."));
    }

    private OnboardingTacheModel chargerTache(Integer id) {
        return taches.findById(id)
                .orElseThrow(() -> new Bloc4Exception("Tâche d'onboarding introuvable."));
    }

    private void verifierResponsable(OnboardingTacheModel t, EmployeModel auteur) {
        if (auteur == null) {
            throw new Bloc4Exception("Aucun utilisateur identifié.");
        }
        boolean assigne = t.getResponsable() != null && t.getResponsable().getId().equals(auteur.getId());
        if (!assigne && !estRh(auteur)) {
            throw new Bloc4Exception("Cette tâche ne vous est pas assignée.");
        }
    }

    private boolean estRh(EmployeModel e) {
        if (e == null) return false;
        return utilisateurs.findByIdEmploye(e.getId())
                .map(UtilisateurModel::getRole).map("RH"::equals).orElse(false);
    }

    private EmployeModel responsablePour(String role, EmployeModel employe, EmployeModel rh) {
        if ("RH".equals(role)) return rh;
        if ("RESPONSABLE".equals(role) && employe.getDepartement() != null) {
            return employes.validateursDuDepartement("RESPONSABLE", employe.getDepartement().getId())
                    .stream().findFirst().orElse(rh);
        }
        if (role == null) return rh;
        return employes.validateursParRole(role).stream().findFirst().orElse(rh);
    }

    private boolean conditionRemplie(String condition, EmployeModel employe, ContratModel contrat) {
        if (condition == null || "TOUJOURS".equals(condition)) return true;
        String code = contrat.getTypeContrat() == null ? "" : contrat.getTypeContrat().getCode();
        if (condition.equals("TYPE_" + code.toUpperCase())) return true;
        if (condition.startsWith("POSTE:")) {
            try {
                Integer id = Integer.parseInt(condition.substring(6));
                return employe.getPoste() != null && id.equals(employe.getPoste().getId());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        if (condition.startsWith("DEPARTEMENT:")) {
            try {
                Integer id = Integer.parseInt(condition.substring(12));
                return employe.getDepartement() != null && id.equals(employe.getDepartement().getId());
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private int specificite(ModeleOnboardingModel m, Integer idPoste, Integer idDepartement) {
        boolean poste = m.getPoste() != null && idPoste != null && idPoste.equals(m.getPoste().getId());
        boolean dept = m.getDepartement() != null && idDepartement != null
                && idDepartement.equals(m.getDepartement().getId());
        if (poste && dept) return 3;
        if (poste) return 2;
        if (dept) return 1;
        return 0;
    }

    private String genererReference() {
        String prefixe = "ONB-" + LocalDate.now().getYear() + "-";
        int numero = onboardings.findFirstByReferenceStartingWithOrderByReferenceDesc(prefixe)
                .map(o -> o.getReference().substring(prefixe.length()))
                .map(s -> {
                    try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
                }).orElse(0) + 1;
        return prefixe + String.format("%04d", numero);
    }

    private void notifierRh(String type, Integer id, String message) {
        employes.validateursParRole("RH").forEach(rh -> notifications.notifier(rh.getId(), type, id, message));
    }

    private OnboardingResume resume(OnboardingModel o, List<OnboardingTacheModel> liste) {
        int total = liste.size();
        int terminees = (int) liste.stream().filter(OnboardingTacheModel::isTerminee).count();
        int retard = (int) liste.stream().filter(OnboardingTacheModel::isEnRetard).count();
        int progression = total == 0 ? 100 : (terminees * 100) / total;
        return new OnboardingResume(o.getId(), o.getReference(),
                o.getEmploye().getNomComplet(),
                o.getEmploye().getPoste() == null ? "—" : o.getEmploye().getPoste().getDesignation(),
                o.getEmploye().getDepartement() == null ? "—" : o.getEmploye().getDepartement().getDesignation(),
                o.getDateDebutPrevue(), progression, retard, terminees, total,
                o.getStatut(), o.getStatutLibelle(), o.getStatutBadge(),
                o.getResponsableRh() == null ? "—" : o.getResponsableRh().getNomComplet());
    }

    private OnboardingVue vue(OnboardingModel o) {
        OnboardingVue v = new OnboardingVue();
        v.setId(o.getId());
        v.setReference(o.getReference());
        v.setIdEmploye(o.getEmploye().getId());
        v.setEmployeNom(o.getEmploye().getNomComplet());
        v.setEmployeMatricule(o.getEmploye().getMatricule());
        v.setPoste(o.getEmploye().getPoste() == null ? "—" : o.getEmploye().getPoste().getDesignation());
        v.setDepartement(o.getEmploye().getDepartement() == null ? "—"
                : o.getEmploye().getDepartement().getDesignation());
        v.setIdContrat(o.getContrat().getId());
        v.setContratReference(o.getContrat().getReference());
        v.setModeleNom(o.getModele().getNom());
        v.setVersionModele(o.getVersionModele());
        v.setDateDebutPrevueFr(Formats.date(o.getDateDebutPrevue()));
        v.setDateDebutReelleFr(Formats.date(o.getDateDebutReelle()));
        v.setDateFinPrevueFr(Formats.date(o.getDateFinPrevue()));
        v.setDateClotureFr(Formats.dateHeure(o.getDateCloture()));
        v.setStatut(o.getStatut());
        v.setStatutLibelle(o.getStatutLibelle());
        v.setStatutBadge(o.getStatutBadge());
        v.setResponsableRhNom(o.getResponsableRh() == null ? "—" : o.getResponsableRh().getNomComplet());
        v.setClotureParNom(o.getCloturePar() == null ? null : o.getCloturePar().getNomComplet());
        v.setMotifAnnulation(o.getMotifAnnulation());
        v.setProgression(o.getProgression());
        return v;
    }

    private TacheVue tacheVue(OnboardingTacheModel t) {
        return new TacheVue(t.getId(), t.getOnboarding().getId(), t.getOnboarding().getReference(),
                t.getOnboarding().getEmploye().getNomComplet(), t.getTitre(), t.getDescription(),
                t.getRoleLibelle(), t.getResponsable() == null ? "—" : t.getResponsable().getNomComplet(),
                t.getDateEcheance(), t.getStatut(), t.getStatutLibelle(), t.getStatutBadge(),
                t.isObligatoire(), t.isBloquante(), t.isEnRetard(), t.isDependancePrete(),
                t.getDependDeTache() == null ? null : t.getDependDeTache().getTitre(),
                t.getCommentaire(), t.getMotifBlocage(), t.getOrdre());
    }

    private List<EvenementAuditVue> historique(Integer id) {
        return audit.historique("ONBOARDING", id).stream()
                .map(e -> new EvenementAuditVue(e.getId(), e.getAction(), e.getAncienneValeur(),
                        e.getNouvelleValeur(), e.getCommentaire(), e.getAuteur().getNomComplet(),
                        Formats.dateHeure(e.getDateEvenement())))
                .toList();
    }
}
