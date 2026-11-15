package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.gestionrh.sirh.model.AbsenceModel;
import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.model.DemandeCongeModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.TypeCongeModel;
import com.gestionrh.sirh.repository.AbsenceRepository;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.DemandeCongeRepository;
import com.gestionrh.sirh.repository.DocumentRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.TypeCongeRepository;
import com.gestionrh.sirh.service.dto.DocumentForm;
import com.gestionrh.sirh.util.Formats;

/**
 * Bloc 7 §7 à §10 — demande de congé : contrôle du solde et des chevauchements, validation manager puis RH,
 * mise à jour du planning (Bloc 6), annulation et interruption.
 */
@Service
public class CongeService {

    private final DemandeCongeRepository demandes;
    private final AbsenceRepository absences;
    private final TypeCongeRepository types;
    private final EmployeRepository employes;
    private final ContratRepository contrats;
    private final DocumentRepository documentsRepo;
    private final DocumentService documents;
    private final SoldeService soldes;
    private final ManagerService managers;
    private final PrevisionService previsions;
    private final PlanningService plannings;
    private final CalculTempsService calcul;
    private final NotificationService notifications;
    private final TempsParametres parametres;

    public CongeService(DemandeCongeRepository demandes, AbsenceRepository absences, TypeCongeRepository types,
                        EmployeRepository employes, ContratRepository contrats, DocumentRepository documentsRepo,
                        DocumentService documents, SoldeService soldes, ManagerService managers,
                        PrevisionService previsions, PlanningService plannings, CalculTempsService calcul,
                        NotificationService notifications, TempsParametres parametres) {
        this.demandes = demandes;
        this.absences = absences;
        this.types = types;
        this.employes = employes;
        this.contrats = contrats;
        this.documentsRepo = documentsRepo;
        this.documents = documents;
        this.soldes = soldes;
        this.managers = managers;
        this.previsions = previsions;
        this.plannings = plannings;
        this.calcul = calcul;
        this.notifications = notifications;
        this.parametres = parametres;
    }

    // ------------------------------------------------------------ lecture

    @Transactional(readOnly = true)
    public List<TypeCongeModel> typesDemandables(boolean rh) {
        return types.findByActifTrueOrderByLibelleAsc().stream()
                .filter(t -> t.isConge() && (rh || t.isSaisieSalarie())).toList();
    }

    @Transactional(readOnly = true)
    public List<DemandeCongeModel> duSalarie(Integer idEmploye) {
        return demandes.findByEmploye_IdOrderByDateDebutDesc(idEmploye);
    }

    /** Demandes en attente d'un manager : les siennes, plus — pour le RH — celles escaladées (RG-DC12). */
    @Transactional(readOnly = true)
    public List<DemandeCongeModel> aValiderParManager(EmployeModel manager) {
        return demandes.findByManager_IdAndStatutOrderByDateDebutAsc(manager.getId(), "SOUMISE");
    }

    @Transactional(readOnly = true)
    public List<DemandeCongeModel> aValiderParRh() {
        return demandes.findByStatutInOrderByDateDebutAsc(List.of("VALIDEE_MANAGER", "SOUMISE")).stream()
                .filter(d -> "VALIDEE_MANAGER".equals(d.getStatut()) || d.isEscaladee()).toList();
    }

    @Transactional(readOnly = true)
    public List<DemandeCongeModel> enCours() {
        return demandes.findByStatutInOrderByDateDebutAsc(List.of("VALIDEE", "EN_COURS"));
    }

    /** RG-DC13 : collègues de la même équipe absents sur la même période (avertissement, non bloquant). */
    @Transactional(readOnly = true)
    public List<String> absentsSimultanes(DemandeCongeModel d) {
        EmployeModel manager = d.getManager();
        if (manager == null) return List.of();
        return managers.equipeDe(manager).stream()
                .filter(e -> !e.getId().equals(d.getEmploye().getId()))
                .filter(e -> !demandes.valideesEntre(e.getId(), d.getDateDebut(), d.getDateFin()).isEmpty()
                        || !absences.activesEntre(e.getId(), d.getDateDebut(), d.getDateFin()).isEmpty())
                .map(EmployeModel::getNomComplet).toList();
    }

    // ------------------------------------------------------------ décompte

    /** RG-DC03 / RG-DC04 : jours décomptés selon le mode du type, demi-journées comprises. */
    public BigDecimal calculerJours(TypeCongeModel type, LocalDate debut, String djDebut, LocalDate fin, String djFin) {
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate d = debut; !d.isAfter(fin); d = d.plusDays(1)) {
            if (!decompte(type, d)) continue;
            BigDecimal part = BigDecimal.ONE;
            if (d.equals(debut) && "APRES_MIDI".equals(djDebut)) part = part.subtract(new BigDecimal("0.5"));
            if (d.equals(fin) && "MATIN".equals(djFin)) part = part.subtract(new BigDecimal("0.5"));
            total = total.add(part.max(BigDecimal.ZERO));
        }
        return total;
    }

    private boolean decompte(TypeCongeModel type, LocalDate d) {
        int jour = d.getDayOfWeek().getValue();
        return switch (type.getModeDecompte()) {
            case "OUVRABLE" -> jour <= 6 && !previsions.estFerie(d);
            case "OUVRE" -> jour <= 5 && !previsions.estFerie(d);
            default -> true;   // CALENDAIRE : tous les jours
        };
    }

    // ------------------------------------------------------------ dépôt

    @Transactional
    public DemandeCongeModel demander(Integer idEmploye, Integer idType, LocalDate debut, String djDebut,
                                      LocalDate fin, String djFin, String motif, MultipartFile justificatif,
                                      EmployeModel saisiPar, boolean rh) {
        EmployeModel e = employes.verrouiller(idEmploye).orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        TypeCongeModel type = types.findById(idType == null ? -1 : idType)
                .filter(TypeCongeModel::isActif).orElseThrow(() -> new Bloc4Exception("Type de congé inconnu (RG-TC01)."));
        if (!type.isConge() || !(rh || type.isSaisieSalarie())) throw new Bloc4Exception("Ce type ne se demande pas ici.");
        djDebut = djDebut == null ? "MATIN" : djDebut;
        djFin = djFin == null ? "APRES_MIDI" : djFin;
        if (debut == null || fin == null || fin.isBefore(debut)) throw new Bloc4Exception("Les dates sont invalides (RG-DC02).");
        if (debut.equals(fin) && "APRES_MIDI".equals(djDebut) && "MATIN".equals(djFin)) {
            throw new Bloc4Exception("La demi-journée de fin ne peut pas précéder celle de début (RG-DC02).");
        }
        if ("SORTI".equals(e.getStatutAdministratif())) throw new Bloc4Exception("Le salarié a quitté l'entreprise.");
        LocalDate finContrat = contrats.findByIdEmployeAndStatutWorkflowInOrderByDateDebutDesc(e.getId(), List.of("EN_VIGUEUR"))
                .stream().map(ContratModel::getDateFin).filter(java.util.Objects::nonNull).findFirst().orElse(null);
        if ((finContrat != null && fin.isAfter(finContrat)) || (e.getDateSortie() != null && fin.isAfter(e.getDateSortie()))) {
            throw new Bloc4Exception("Le congé dépasse la fin du contrat ou la date de sortie (RG-DC07).");
        }
        if (!type.isUrgent() && debut.isBefore(LocalDate.now().plusDays(type.getDelaiPrevenanceJours()))) {
            throw new Bloc4Exception("Délai de prévenance non respecté : ce congé se demande au moins "
                    + type.getDelaiPrevenanceJours() + " jour(s) à l'avance (RG-DC06).");
        }
        BigDecimal nb = calculerJours(type, debut, djDebut, fin, djFin);
        if (nb.signum() <= 0) throw new Bloc4Exception("La période ne contient aucun jour décompté.");
        if (type.getDureeMaxJours() != null && nb.compareTo(BigDecimal.valueOf(type.getDureeMaxJours())) > 0) {
            throw new Bloc4Exception("Durée maximale de « " + type.getLibelle() + " » : " + type.getDureeMaxJours() + " jours.");
        }
        controlerChevauchement(e, debut, djDebut, fin, djFin, null);
        if (type.getTypeSolde() != null) {                                              // RG-DC05
            BigDecimal dispo = soldes.solde(e.getId(), type.getTypeSolde()).getDisponible();
            if (dispo.compareTo(nb) < 0) {
                throw new Bloc4Exception("Solde insuffisant : " + dispo.stripTrailingZeros().toPlainString()
                        + " j disponible(s) pour " + nb.stripTrailingZeros().toPlainString() + " j demandé(s) (RG-DC05).");
            }
        }
        DemandeCongeModel d = new DemandeCongeModel();
        d.setReference(reference(debut));
        d.setEmploye(e);
        d.setType(type);
        d.setDateDebut(debut);
        d.setDemiJourneeDebut(djDebut);
        d.setDateFin(fin);
        d.setDemiJourneeFin(djFin);
        d.setNbJours(nb);
        d.setMotif(motif == null || motif.isBlank() ? null : motif.trim());
        d.setSaisiPar(saisiPar);
        if (justificatif != null && !justificatif.isEmpty()) d.setIdJustificatif(deposer(e, type, justificatif, saisiPar));
        EmployeModel manager = type.isValidationManager() ? managers.managerDe(e) : null;
        d.setManager(manager);
        d.setStatut(manager == null ? "VALIDEE_MANAGER" : "SOUMISE");                    // RG-DC10 : types « RH seul »
        try {
            d = demandes.saveAndFlush(d);
        } catch (DataIntegrityViolationException ex) {
            throw new Bloc4Exception("Ces dates chevauchent un autre congé (RG-CH05).");
        }
        if (manager != null) {
            notifications.notifier(manager.getId(), "CONGE", d.getId(), e.getNomComplet() + " demande "
                    + type.getLibelle().toLowerCase() + " du " + d.getPeriodeLibelle() + " (" + d.getNbJoursFr() + ").");
        } else {
            notifierRh(d, "Demande " + d.getReference() + " (" + type.getLibelle() + ") de " + e.getNomComplet() + " à valider.");
        }
        return d;
    }

    /** RG-CH01 à RG-CH04 : aucune demi-journée commune avec un congé actif ni une absence. */
    private void controlerChevauchement(EmployeModel e, LocalDate debut, String djDebut, LocalDate fin, String djFin, Integer exclu) {
        int a1 = demiJournee(debut, djDebut, true), b1 = demiJournee(fin, djFin, false);
        for (DemandeCongeModel o : demandes.actives(e.getId(), debut, fin, DemandeCongeModel.ACTIFS, exclu)) {
            int a2 = demiJournee(o.getDateDebut(), o.getDemiJourneeDebut(), true);
            int b2 = demiJournee(o.getDateFin(), o.getDemiJourneeFin(), false);
            if (a1 < b2 && a2 < b1) {
                throw new Bloc4Exception("Dates en conflit avec la demande " + o.getReference() + " (" + o.getPeriodeLibelle() + ") — RG-CH01.");
            }
        }
        for (AbsenceModel a : absences.activesEntre(e.getId(), debut, fin)) {
            throw new Bloc4Exception("Dates en conflit avec une absence enregistrée (" + a.getPeriodeLibelle() + ") — RG-CH01.");
        }
    }

    /** Index de demi-journée : début inclus, fin exclue (même convention que la colonne « plage » en base). */
    private static int demiJournee(LocalDate d, String moitie, boolean debut) {
        int base = (int) d.toEpochDay() * 2;
        return debut ? base + ("APRES_MIDI".equals(moitie) ? 1 : 0) : base + ("MATIN".equals(moitie) ? 1 : 2);
    }

    // ------------------------------------------------------------ validation

    @Transactional
    public void validerManager(Integer id, String commentaire, EmployeModel valideur, boolean rh) {
        DemandeCongeModel d = charger(id);
        if (!"SOUMISE".equals(d.getStatut())) throw new Bloc4Exception("Cette demande n'attend pas le manager.");
        verifierManager(d, valideur, rh);
        d.setStatut("VALIDEE_MANAGER");
        d.setValideurManager(valideur);
        d.setDateValidationManager(LocalDateTime.now());
        d.setCommentaireManager(commentaire);
        demandes.save(d);
        notifierRh(d, "Demande " + d.getReference() + " de " + d.getEmploye().getNomComplet() + " validée par le manager : validation RH attendue.");
    }

    /** RG-DC10 : validation finale RH (conformité, solde, justificatif — RG-TC03). */
    @Transactional
    public void validerRh(Integer id, String commentaire, EmployeModel rh) {
        DemandeCongeModel d = charger(id);
        if (!"VALIDEE_MANAGER".equals(d.getStatut())) throw new Bloc4Exception("Cette demande n'attend pas le RH.");
        if (d.getEmploye().getId().equals(rh.getId())) throw new Bloc4Exception("Vous ne pouvez pas valider votre propre demande (RG-DC09).");
        if (d.getType().isJustificatifObligatoire()) {
            if (d.getIdJustificatif() == null) {
                throw new Bloc4Exception("Le justificatif est obligatoire pour « " + d.getType().getLibelle() + " » (RG-TC03).");
            }
            documentsRepo.findById(d.getIdJustificatif()).filter(doc -> "RECU".equals(doc.getStatutValidation()))
                    .ifPresent(doc -> documents.verifier(doc.getId(), rh));
        }
        if (d.getType().getTypeSolde() != null
                && soldes.solde(d.getEmploye().getId(), d.getType().getTypeSolde()).getDisponible().signum() < 0) {
            throw new Bloc4Exception("Solde devenu insuffisant depuis la demande (RG-DC05).");
        }
        d.setStatut("VALIDEE");
        d.setValideurRh(rh);
        d.setDateValidationRh(LocalDateTime.now());
        d.setCommentaireRh(commentaire);
        demandes.save(d);
        appliquerAuTemps(d);
        avancer(d, LocalDate.now());
        notifications.notifier(d.getEmploye().getId(), "CONGE", id, "Votre congé du " + d.getPeriodeLibelle() + " est validé.");
        if (d.getManager() != null) {
            notifications.notifier(d.getManager().getId(), "CONGE", id, "Congé de " + d.getEmploye().getNomComplet() + " validé : " + d.getPeriodeLibelle() + ".");
        }
    }

    /** RG-DC11 : refus motivé, jours libérés (RG-SC06). */
    @Transactional
    public void refuser(Integer id, String motif, EmployeModel valideur, boolean rh) {
        if (motif == null || motif.isBlank()) throw new Bloc4Exception("Le motif du refus est obligatoire (RG-DC11).");
        DemandeCongeModel d = charger(id);
        if ("SOUMISE".equals(d.getStatut())) {
            verifierManager(d, valideur, rh);
        } else if ("VALIDEE_MANAGER".equals(d.getStatut())) {
            if (!rh) throw new Bloc4Exception("Seul le RH décide à cette étape.");
        } else {
            throw new Bloc4Exception("Cette demande n'est plus en attente.");
        }
        if (d.getEmploye().getId().equals(valideur.getId())) throw new Bloc4Exception("Vous ne pouvez pas traiter votre propre demande.");
        d.setStatut("REFUSEE");
        d.setMotifRefus(motif.trim() + " (" + valideur.getNomComplet() + ")");
        demandes.save(d);
        notifications.notifier(d.getEmploye().getId(), "CONGE", id, "Congé du " + d.getPeriodeLibelle() + " refusé : " + motif);
    }

    /** RG-MO01 : annulation par le salarié avant le début ; les jours réservés sont libérés. */
    @Transactional
    public void annuler(Integer id, String motif, EmployeModel salarie) {
        DemandeCongeModel d = charger(id);
        if (!d.getEmploye().getId().equals(salarie.getId())) throw new Bloc4Exception("Cette demande ne vous appartient pas.");
        if (!d.isAnnulableParSalarie()) throw new Bloc4Exception("Un congé commencé ne s'annule pas : demandez une interruption au RH.");
        boolean etaitValidee = "VALIDEE".equals(d.getStatut());
        d.setStatut("ANNULEE");
        d.setMotifAnnulation(motif == null || motif.isBlank() ? "Annulée par le salarié" : motif.trim());
        demandes.save(d);
        if (etaitValidee) {
            appliquerAuTemps(d);
            notifierRh(d, "Congé " + d.getReference() + " de " + d.getEmploye().getNomComplet() + " annulé.");
            if (d.getManager() != null) {
                notifications.notifier(d.getManager().getId(), "CONGE", id, "Congé de " + d.getEmploye().getNomComplet() + " annulé (" + d.getPeriodeLibelle() + ").");
            }
        }
    }

    /** RG-MO03 / RG-MO04 : interruption par le RH ; les jours non pris sont recrédités. */
    @Transactional
    public void interrompre(Integer id, LocalDate dernierJour, String motif, EmployeModel rh) {
        DemandeCongeModel d = charger(id);
        if (!"EN_COURS".equals(d.getStatut())) throw new Bloc4Exception("Seul un congé en cours s'interrompt.");
        if (motif == null || motif.isBlank()) throw new Bloc4Exception("Le motif de l'interruption est obligatoire.");
        if (dernierJour == null || dernierJour.isBefore(d.getDateDebut()) || !dernierJour.isBefore(d.getDateFin())) {
            throw new Bloc4Exception("Le dernier jour de congé doit être compris dans la période et avant la fin prévue.");
        }
        LocalDate ancienneFin = d.getDateFin();
        d.setDateFin(dernierJour);
        d.setDemiJourneeFin("APRES_MIDI");
        BigDecimal pris = calculerJours(d.getType(), d.getDateDebut(), d.getDemiJourneeDebut(), dernierJour, "APRES_MIDI");
        d.setJoursPris(pris);
        d.setNbJours(pris.signum() > 0 ? pris : new BigDecimal("0.5"));
        d.setStatut("INTERROMPUE");
        d.setMotifAnnulation("Interrompu le " + Formats.date(dernierJour) + " : " + motif.trim());
        demandes.save(d);
        soldes.debiter(d, pris);
        plannings.synchroniserConge(d.getEmploye(), dernierJour.plusDays(1), ancienneFin);
        calcul.recalculer(d.getEmploye(), dernierJour.plusDays(1), ancienneFin);
        notifications.notifier(d.getEmploye().getId(), "CONGE", id, "Votre congé est interrompu au " + Formats.date(dernierJour) + " : " + motif);
    }

    /** Traitement quotidien : VALIDEE → EN_COURS → PRISE avec débit définitif du solde. */
    @Transactional
    public int avancerStatuts(LocalDate jour) {
        int n = 0;
        for (DemandeCongeModel d : enCours()) {
            if (avancer(d, jour)) n++;
        }
        return n;
    }

    private boolean avancer(DemandeCongeModel d, LocalDate jour) {
        boolean change = false;
        if ("VALIDEE".equals(d.getStatut()) && !d.getDateDebut().isAfter(jour)) {
            d.setStatut("EN_COURS");
            change = true;
        }
        if ("EN_COURS".equals(d.getStatut()) && d.getDateFin().isBefore(jour)) {
            d.setStatut("PRISE");
            d.setJoursPris(d.getNbJours());
            soldes.debiter(d, d.getNbJours());
            change = true;
        }
        if (change) demandes.save(d);
        return change;
    }

    /** RG-DC12 : demande sans réponse du manager dans le délai → escaladée au RH. */
    @Transactional
    public int escalader() {
        List<DemandeCongeModel> liste = demandes.aEscalader(LocalDateTime.now().minusDays(parametres.delaiValidationManagerJours));
        for (DemandeCongeModel d : liste) {
            d.setEscaladee(true);
            demandes.save(d);
            notifierRh(d, "Demande " + d.getReference() + " sans réponse du manager depuis "
                    + parametres.delaiValidationManagerJours + " jours : le RH peut la traiter.");
        }
        return liste.size();
    }

    // ------------------------------------------------------------ utilitaires

    /** Bloc 7 → Bloc 6 : planning et journées passées recalculés (RG-PP01, RG-PP02, RG-TT06). */
    private void appliquerAuTemps(DemandeCongeModel d) {
        plannings.synchroniserConge(d.getEmploye(), d.getDateDebut(), d.getDateFin());
        calcul.recalculer(d.getEmploye(), d.getDateDebut(), d.getDateFin());
    }

    private void verifierManager(DemandeCongeModel d, EmployeModel valideur, boolean rh) {
        if (d.getEmploye().getId().equals(valideur.getId())) {
            throw new Bloc4Exception("Vous ne pouvez pas valider votre propre demande (RG-DC09).");
        }
        boolean estManager = d.getManager() != null && d.getManager().getId().equals(valideur.getId());
        if (!estManager && !(rh && d.isEscaladee())) {
            throw new Bloc4Exception("Seul le manager du salarié valide cette étape (RG-DC08)"
                    + (rh ? " ; le RH la reprend après escalade (RG-DC12)." : "."));
        }
    }

    private Integer deposer(EmployeModel e, TypeCongeModel type, MultipartFile fichier, EmployeModel auteur) {
        DocumentForm f = new DocumentForm();
        f.setIdEmploye(e.getId());
        if (type.getTypeJustificatif() != null) {
            f.setIdTypeAdministratif(type.getTypeJustificatif().getId());
        } else {
            f.setTypeDocument("ADMINISTRATIF");
            f.setTitre("Justificatif — " + type.getLibelle());
        }
        f.setCommentaire("Justificatif : " + type.getLibelle());
        return documents.enregistrer(fichier, f, auteur).getId();
    }

    @Transactional
    public void ajouterJustificatif(Integer id, MultipartFile fichier, EmployeModel auteur) {
        DemandeCongeModel d = charger(id);
        if (!d.getEmploye().getId().equals(auteur.getId())) throw new Bloc4Exception("Cette demande ne vous appartient pas.");
        if (!List.of("SOUMISE", "VALIDEE_MANAGER").contains(d.getStatut())) throw new Bloc4Exception("La demande n'est plus en attente.");
        if (fichier == null || fichier.isEmpty()) throw new Bloc4Exception("Choisissez un fichier.");
        d.setIdJustificatif(deposer(d.getEmploye(), d.getType(), fichier, auteur));
        demandes.save(d);
    }

    private void notifierRh(DemandeCongeModel d, String message) {
        employes.validateursParRole("RH").stream().filter(r -> !r.getId().equals(d.getEmploye().getId()))
                .forEach(r -> notifications.notifier(r.getId(), "CONGE", d.getId(), message));
    }

    private DemandeCongeModel charger(Integer id) {
        return demandes.findById(id).orElseThrow(() -> new Bloc4Exception("Demande introuvable."));
    }

    private String reference(LocalDate debut) {
        String prefixe = "CGE-" + debut.getYear() + "-";
        return prefixe + String.format("%04d", demandes.compterReferences(prefixe) + 1);
    }
}
