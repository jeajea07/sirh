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
import com.gestionrh.sirh.model.DemandeCongeModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.TypeCongeModel;
import com.gestionrh.sirh.repository.AbsenceRepository;
import com.gestionrh.sirh.repository.DemandeCongeRepository;
import com.gestionrh.sirh.repository.DocumentRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.TypeCongeRepository;
import com.gestionrh.sirh.service.dto.DocumentForm;
import com.gestionrh.sirh.util.Formats;

/** Bloc 7 §9 — absences : déclaration, justificatif, décision RH (RG-AB). */
@Service
public class AbsenceService {

    /** RG-AB08 : au-delà, le RH est invité à suspendre le contrat (Bloc 5). */
    private static final int SEUIL_ABSENCE_LONGUE = 30;

    private final AbsenceRepository absences;
    private final DemandeCongeRepository demandes;
    private final TypeCongeRepository types;
    private final EmployeRepository employes;
    private final DocumentRepository documentsRepo;
    private final DocumentService documents;
    private final SoldeService soldes;
    private final CongeService conges;
    private final ManagerService managers;
    private final PlanningService plannings;
    private final CalculTempsService calcul;
    private final NotificationService notifications;

    public AbsenceService(AbsenceRepository absences, DemandeCongeRepository demandes, TypeCongeRepository types,
                          EmployeRepository employes, DocumentRepository documentsRepo, DocumentService documents,
                          SoldeService soldes, CongeService conges, ManagerService managers, PlanningService plannings,
                          CalculTempsService calcul, NotificationService notifications) {
        this.absences = absences;
        this.demandes = demandes;
        this.types = types;
        this.employes = employes;
        this.documentsRepo = documentsRepo;
        this.documents = documents;
        this.soldes = soldes;
        this.conges = conges;
        this.managers = managers;
        this.plannings = plannings;
        this.calcul = calcul;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<TypeCongeModel> typesDeclarables(boolean rh) {
        return types.findByActifTrueOrderByLibelleAsc().stream()
                .filter(t -> !t.isConge() && (rh || t.isSaisieSalarie())).toList();
    }

    @Transactional(readOnly = true)
    public List<AbsenceModel> duSalarie(Integer idEmploye) {
        return absences.findByEmploye_IdOrderByDateDebutDesc(idEmploye);
    }

    @Transactional(readOnly = true)
    public List<AbsenceModel> aDecider() {
        return absences.findByStatutInOrderByDateDebutAsc(List.of("DECLAREE", "EN_ATTENTE_JUSTIFICATIF"));
    }

    @Transactional
    public AbsenceModel declarer(Integer idEmploye, Integer idType, LocalDate debut, LocalDate fin, String commentaire,
                                 MultipartFile justificatif, EmployeModel saisiPar, boolean rh) {
        EmployeModel e = employes.verrouiller(idEmploye).orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
        TypeCongeModel type = types.findById(idType == null ? -1 : idType).filter(TypeCongeModel::isActif)
                .orElseThrow(() -> new Bloc4Exception("Type d'absence inconnu (RG-TC01)."));
        if (type.isConge() || !(rh || type.isSaisieSalarie())) throw new Bloc4Exception("Ce type ne se déclare pas ici.");
        if (debut == null || fin == null || fin.isBefore(debut)) throw new Bloc4Exception("Les dates sont invalides.");
        if (!demandes.actives(e.getId(), debut, fin, DemandeCongeModel.ACTIFS, null).isEmpty()
                || !absences.activesEntre(e.getId(), debut, fin).isEmpty()) {
            throw new Bloc4Exception("Ces dates chevauchent un congé ou une absence déjà enregistrés (RG-CH01).");
        }
        AbsenceModel a = new AbsenceModel();
        a.setEmploye(e);
        a.setType(type);
        a.setDateDebut(debut);
        a.setDateFin(fin);
        a.setOrigine(rh ? "SAISIE_RH" : "DECLAREE_SALARIE");
        a.setCommentaire(commentaire == null || commentaire.isBlank() ? null : commentaire.trim());
        a.setSaisiPar(saisiPar);
        if (type.isJustificatifObligatoire()) {                                          // RG-AB02
            a.setDateLimiteJustificatif(LocalDate.now().plusDays(type.getDelaiJustificatifJours()));
        }
        if (justificatif != null && !justificatif.isEmpty()) a.setIdJustificatif(deposer(e, type, justificatif, saisiPar));
        try {
            a = absences.saveAndFlush(a);
        } catch (DataIntegrityViolationException ex) {
            throw new Bloc4Exception("Ces dates chevauchent une autre absence (RG-CH05).");
        }
        appliquerAuTemps(a);
        // RG-AB07 : le manager est informé sans le motif médical.
        EmployeModel manager = managers.managerDe(e);
        if (manager != null) {
            notifications.notifier(manager.getId(), "ABSENCE", a.getId(), e.getNomComplet() + " est absent(e) : " + a.getPeriodeLibelle() + ".");
        }
        String msg = e.getNomComplet() + " : " + type.getLibelle().toLowerCase() + " du " + a.getPeriodeLibelle()
                + (a.getIdJustificatif() == null && type.isJustificatifObligatoire() ? " — justificatif attendu avant le " + a.getDateLimiteFr() : "") + ".";
        employes.validateursParRole("RH").forEach(r -> notifications.notifier(r.getId(), "ABSENCE", null, msg));
        if (a.getNbJours() > SEUIL_ABSENCE_LONGUE) {
            employes.validateursParRole("RH").forEach(r -> notifications.notifier(r.getId(), "SALARIE", e.getId(),
                    "Absence de plus de " + SEUIL_ABSENCE_LONGUE + " jours pour " + e.getNomComplet() + " : envisager la suspension (RG-AB08)."));
        }
        return a;
    }

    @Transactional
    public void deposerJustificatif(Integer id, MultipartFile fichier, EmployeModel auteur, boolean rh) {
        AbsenceModel a = charger(id);
        if (!rh && !a.getEmploye().getId().equals(auteur.getId())) throw new Bloc4Exception("Cette absence ne vous concerne pas.");
        if (!a.isEnAttente()) throw new Bloc4Exception("Cette absence est déjà traitée.");
        if (fichier == null || fichier.isEmpty()) throw new Bloc4Exception("Choisissez un fichier.");
        a.setIdJustificatif(deposer(a.getEmploye(), a.getType(), fichier, auteur));
        a.setStatut("DECLAREE");
        absences.save(a);
    }

    /** Décision RH : JUSTIFIER, INJUSTIFIEE, CONVERTIR (en congé payé, accord du salarié requis — RG-AB05) ou ANNULER. */
    @Transactional
    public void decider(Integer id, String decision, String commentaire, boolean accordSalarie, EmployeModel rh) {
        AbsenceModel a = charger(id);
        if (!a.isEnAttente()) throw new Bloc4Exception("Cette absence est déjà traitée.");
        if (a.getEmploye().getId().equals(rh.getId())) throw new Bloc4Exception("Vous ne pouvez pas traiter votre propre absence.");
        switch (decision == null ? "" : decision) {
            case "JUSTIFIER" -> {
                if (a.getType().isJustificatifObligatoire() && a.getIdJustificatif() == null) {
                    throw new Bloc4Exception("Le justificatif est obligatoire pour « " + a.getType().getLibelle() + " » (RG-TC03).");
                }
                if (a.getIdJustificatif() != null) {
                    documentsRepo.findById(a.getIdJustificatif()).filter(d -> "RECU".equals(d.getStatutValidation()))
                            .ifPresent(d -> documents.verifier(d.getId(), rh));
                }
                a.setStatut("JUSTIFIEE");
            }
            case "INJUSTIFIEE" -> {
                exiger(commentaire, "Motivez la décision.");
                a.setStatut("INJUSTIFIEE");
            }
            case "CONVERTIR" -> {
                if (!accordSalarie) throw new Bloc4Exception("La conversion en congé payé exige l'accord du salarié (RG-AB05).");
                convertir(a, rh);
            }
            case "ANNULER" -> {
                exiger(commentaire, "Motivez l'annulation.");
                a.setStatut("ANNULEE");
            }
            default -> throw new Bloc4Exception("Décision inconnue.");
        }
        a.setDecisionCommentaire(commentaire == null || commentaire.isBlank() ? null : commentaire.trim());
        a.setDecideur(rh);
        a.setDateDecision(LocalDateTime.now());
        absences.save(a);
        appliquerAuTemps(a);
        notifications.notifier(a.getEmploye().getId(), "ABSENCE", id, "Votre absence du " + a.getPeriodeLibelle() + " : " + a.getStatutLibelle().toLowerCase() + ".");
    }

    /** RG-AB05 : jamais de déduction automatique ; la conversion crée un congé payé validé et débite le solde. */
    private void convertir(AbsenceModel a, EmployeModel rh) {
        TypeCongeModel cp = types.findByCode("CONGE_PAYE").orElseThrow(() -> new Bloc4Exception("Type CONGE_PAYE absent."));
        BigDecimal nb = conges.calculerJours(cp, a.getDateDebut(), "MATIN", a.getDateFin(), "APRES_MIDI");
        BigDecimal dispo = soldes.solde(a.getEmploye().getId(), "CONGE_PAYE").getDisponible();
        if (dispo.compareTo(nb) < 0) throw new Bloc4Exception("Solde de congés payés insuffisant pour la conversion.");
        a.setStatut("CONVERTIE");
        absences.saveAndFlush(a);
        DemandeCongeModel d = new DemandeCongeModel();
        d.setReference("CGE-" + a.getDateDebut().getYear() + "-ABS" + a.getId());
        d.setEmploye(a.getEmploye());
        d.setType(cp);
        d.setDateDebut(a.getDateDebut());
        d.setDateFin(a.getDateFin());
        d.setNbJours(nb);
        d.setMotif("Conversion de l'absence du " + a.getPeriodeLibelle() + " avec l'accord du salarié");
        d.setStatut(a.getDateFin().isBefore(LocalDate.now()) ? "PRISE" : "VALIDEE");
        d.setJoursPris("PRISE".equals(d.getStatut()) ? nb : null);
        d.setValideurRh(rh);
        d.setDateValidationRh(LocalDateTime.now());
        d.setSaisiPar(rh);
        try {
            d = demandes.saveAndFlush(d);
        } catch (DataIntegrityViolationException ex) {
            throw new Bloc4Exception("Un congé existe déjà sur ces dates (RG-CH05).");
        }
        if ("PRISE".equals(d.getStatut())) soldes.debiter(d, nb);
        a.setIdDemandeConge(d.getId());
    }

    /** RG-AB03 : justificatif non reçu dans le délai → en attente, relance du salarié et du RH. */
    @Transactional
    public int relancerJustificatifs(LocalDate jour) {
        List<AbsenceModel> liste = absences.justificatifEnRetard(jour);
        for (AbsenceModel a : liste) {
            if (a.getIdJustificatif() != null) continue;
            a.setStatut("EN_ATTENTE_JUSTIFICATIF");
            absences.save(a);
            notifications.notifier(a.getEmploye().getId(), "ABSENCE", a.getId(),
                    "Justificatif non reçu pour votre absence du " + a.getPeriodeLibelle() + " : déposez-le depuis « Congés ».");
            employes.validateursParRole("RH").forEach(r -> notifications.notifier(r.getId(), "ABSENCE", a.getId(),
                    "Justificatif en retard : " + a.getEmploye().getNomComplet() + " (" + a.getPeriodeLibelle() + ")."));
        }
        return liste.size();
    }

    private void appliquerAuTemps(AbsenceModel a) {
        plannings.synchroniserConge(a.getEmploye(), a.getDateDebut(), a.getDateFin());
        calcul.recalculer(a.getEmploye(), a.getDateDebut(), a.getDateFin());
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
        f.setCommentaire("Absence : " + type.getLibelle() + " — " + Formats.date(LocalDate.now()));
        return documents.enregistrer(fichier, f, auteur).getId();
    }

    private static void exiger(String v, String message) {
        if (v == null || v.isBlank()) throw new Bloc4Exception(message);
    }

    private AbsenceModel charger(Integer id) {
        return absences.findById(id).orElseThrow(() -> new Bloc4Exception("Absence introuvable."));
    }
}
