package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.dto.ControleBudget;
import com.gestionrh.sirh.dto.ControleSalaire;
import com.gestionrh.sirh.dto.DemandeFinanceResume;
import com.gestionrh.sirh.dto.HistoriqueRhLigne;
import com.gestionrh.sirh.model.BudgetDepartementModel;
import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.HistoriqueModificationModel;
import com.gestionrh.sirh.model.NotificationModel;
import com.gestionrh.sirh.model.ValidationEtapeModel;
import com.gestionrh.sirh.repository.BudgetDepartementRepository;
import com.gestionrh.sirh.repository.DemandeBesoinRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.HistoriqueModificationRepository;
import com.gestionrh.sirh.repository.NotificationRepository;
import com.gestionrh.sirh.repository.ValidationEtapeRepository;

/** Étape « Finance » : contrôle du coût annuel de la demande par rapport au budget du département. */
@Service
public class FinanceService {

    public static final String ACTION_CORRIGER = "corriger";
    public static final String ACTION_VALIDER = "valider";
    public static final String ACTION_RENVOYER = "renvoyer";
    public static final String ACTION_REJETER = "rejeter";

    private static final String ETAPE_FINANCE = "FINANCE";
    private static final String ETAPE_RH = "RH";
    private static final String ETAPE_SUIVANTE = "DG";
    private static final int MOIS_PAR_AN = 12;
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter JOUR_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DemandeBesoinRepository demandes;
    private final ValidationEtapeRepository validations;
    private final HistoriqueModificationRepository historiques;
    private final NotificationRepository notifications;
    private final EmployeRepository employes;
    private final BudgetDepartementRepository budgets;

    public FinanceService(DemandeBesoinRepository demandes, ValidationEtapeRepository validations,
                          HistoriqueModificationRepository historiques, NotificationRepository notifications,
                          EmployeRepository employes, BudgetDepartementRepository budgets) {
        this.demandes = demandes;
        this.validations = validations;
        this.historiques = historiques;
        this.notifications = notifications;
        this.employes = employes;
        this.budgets = budgets;
    }

    @Transactional(readOnly = true)
    public List<DemandeFinanceResume> aVerifier() {
        List<DemandeFinanceResume> resultat = new ArrayList<>();
        for (ValidationEtapeModel v : validations.findByEtapeAndDecisionIsNullOrderByDateReceptionAsc(ETAPE_FINANCE)) {
            DemandeBesoinModel d = demandes.findById(v.getIdDemandeBesoin()).orElse(null);
            if (d == null || !estActive(d)) {
                continue;
            }
            resultat.add(new DemandeFinanceResume(d.getId(), d.getNumero(),
                    d.getPoste() == null ? null : d.getPoste().getDesignation(),
                    d.getDepartement().getDesignation(), nomEmploye(d.getIdDemandeur()),
                    d.getTypeContrat() == null ? "-" : d.getTypeContrat().getCode(),
                    d.getMotifLibelle(), v.getDateReception().format(JOUR),
                    controlerBudget(d, d.getSalairePropose())));
        }
        return resultat;
    }

    /** Les décisions prises à l'étape Finance, la plus récente d'abord. {@code idFinance} null = toutes (vue du DG). */
    @Transactional(readOnly = true)
    public List<HistoriqueRhLigne> historique(Integer idFinance) {
        List<HistoriqueRhLigne> resultat = new ArrayList<>();
        List<ValidationEtapeModel> decisions = idFinance == null
                ? validations.findByEtapeAndDecisionIsNotNullOrderByDateDecisionDesc(ETAPE_FINANCE)
                : validations.findByEtapeAndIdValidateurAndDecisionIsNotNullOrderByDateDecisionDesc(ETAPE_FINANCE, idFinance);
        for (ValidationEtapeModel v : decisions) {
            DemandeBesoinModel d = demandes.findById(v.getIdDemandeBesoin()).orElse(null);
            if (d == null) {
                continue;
            }
            resultat.add(new HistoriqueRhLigne(d.getId(), d.getNumero(),
                    d.getPoste() == null ? null : d.getPoste().getDesignation(),
                    d.getDepartement().getDesignation(), nomEmploye(d.getIdDemandeur()),
                    d.getTypeContrat() == null ? "-" : d.getTypeContrat().getCode(),
                    d.getSalaireFr(), v.getDecision(), v.getCommentaire(),
                    v.getDateDecision() == null ? "" : v.getDateDecision().format(JOUR_HEURE),
                    d.getStatutLibelle(), d.getStatutBadge()));
        }
        return resultat;
    }

    @Transactional(readOnly = true)
    public VerificationFinanceForm formulaireDe(Integer id) {
        DemandeBesoinModel d = chargerEnAttente(id);
        VerificationFinanceForm f = new VerificationFinanceForm();
        f.setSalairePropose(d.getSalairePropose());
        f.setDureeMois(d.getDureeMois());
        return f;
    }

    @Transactional(readOnly = true)
    public VerificationFinance preparer(Integer id, VerificationFinanceForm saisie) {
        DemandeBesoinModel d = chargerEnAttente(id);
        ValidationEtapeModel v = etapeEnAttente(id);
        BigDecimal salaire = saisie != null ? saisie.getSalairePropose() : d.getSalairePropose();
        boolean dureeRequise = d.getTypeContrat() != null && d.getTypeContrat().isAvecDuree();
        return new VerificationFinance(d, nomEmploye(d.getIdDemandeur()), v.getDateReception().format(JOUR),
                controlerBudget(d, salaire), dureeRequise);
    }

    @Transactional(readOnly = true)
    public List<String> verifier(Integer id, String action, VerificationFinanceForm f) {
        DemandeBesoinModel d = chargerEnAttente(id);
        List<String> erreurs = new ArrayList<>();

        if (ACTION_RENVOYER.equals(action) || ACTION_REJETER.equals(action)) {
            if (f.getCommentaire() == null || f.getCommentaire().isBlank()) {
                erreurs.add(ACTION_RENVOYER.equals(action)
                        ? "Un commentaire est obligatoire pour renvoyer la demande au demandeur."
                        : "Un motif est obligatoire pour rejeter la demande.");
            }
            return erreurs;
        }
        if (!ACTION_CORRIGER.equals(action) && !ACTION_VALIDER.equals(action)) {
            erreurs.add("Action inconnue.");
            return erreurs;
        }

        if (f.getSalairePropose() == null || f.getSalairePropose().signum() <= 0) {
            erreurs.add("Le salaire proposé doit être supérieur à 0.");
        }
        if (d.getTypeContrat() != null && d.getTypeContrat().isAvecDuree()
                && (f.getDureeMois() == null || f.getDureeMois() < 1)) {
            erreurs.add("La durée du contrat (en mois) est obligatoire pour un " + d.getTypeContrat().getCode() + ".");
        }
        // Un dépassement de budget n'empêche pas la validation : la Finance décide.
        return erreurs;
    }

    @Transactional
    public String decider(Integer id, String action, VerificationFinanceForm f, EmployeModel finance) {
        DemandeBesoinModel d = chargerEnAttente(id);
        ValidationEtapeModel etape = etapeEnAttente(id);
        String commentaire = nettoyer(f.getCommentaire());

        if (ACTION_CORRIGER.equals(action)) {
            appliquerCorrections(d, f, finance);
            demandes.save(d);
            return "Corrections enregistrées.";
        }

        if (ACTION_VALIDER.equals(action)) {
            appliquerCorrections(d, f, finance);
            cloturer(etape, "VALIDEE", commentaire, finance);
            d.setStatut("EN_VERIFICATION");
            demandes.save(d);

            ValidationEtapeModel suivante = new ValidationEtapeModel();
            suivante.setIdDemandeBesoin(id);
            suivante.setEtape(ETAPE_SUIVANTE);
            suivante.setDateReception(LocalDateTime.now());
            validations.save(suivante);

            String message = "Nouvelle demande à traiter : " + d.getNumero() + " — " + d.getPoste().getDesignation()
                    + " (" + d.getDepartement().getDesignation() + ")";
            for (EmployeModel e : employes.validateursParRole(ETAPE_SUIVANTE)) {
                notifier(e.getId(), d, message);
            }
            return "La demande " + d.getNumero() + " a été validée et envoyée à la Direction générale.";
        }

        if (ACTION_RENVOYER.equals(action)) {
            cloturer(etape, "RENVOYEE", commentaire, finance);
            d.setStatut("A_CORRIGER");
            demandes.save(d);
            notifier(d.getIdDemandeur(), d, "Votre demande " + d.getNumero()
                    + " a été renvoyée pour correction par la Finance. Commentaire : " + commentaire);
            return "La demande " + d.getNumero() + " a été renvoyée au demandeur pour correction.";
        }

        if (ACTION_REJETER.equals(action)) {
            cloturer(etape, "REJETEE", commentaire, finance);
            d.setStatut("REJETEE");
            demandes.save(d);
            notifier(d.getIdDemandeur(), d, "Votre demande " + d.getNumero()
                    + " a été rejetée par la Finance. Motif : " + commentaire);
            return "La demande " + d.getNumero() + " a été rejetée.";
        }
        throw new DemandeException("Action inconnue.");
    }

    /**
     * Compare le coût annuel (salaire × 12 × nombre de postes) au budget disponible du département pour l'année
     * souhaitée. Un remplacement n'est pas compté : le poste est déjà budgété.
     */
    public ControleBudget controlerBudget(DemandeBesoinModel d, BigDecimal salaire) {
        int annee = d.getDateSouhaitee() != null ? d.getDateSouhaitee().getYear() : LocalDate.now().getYear();
        BudgetDepartementModel budget = budgets
                .findFirstByIdDepartementAndPeriodeStartingWith(d.getDepartement().getId(), String.valueOf(annee))
                .orElse(null);
        if (budget == null) {
            return new ControleBudget(ControleBudget.SANS_BUDGET, annee, null, null, null);
        }

        BigDecimal engage = BigDecimal.ZERO;
        for (DemandeBesoinModel autre : demandes.demandesEngagees(d.getDepartement().getId(), d.getId())) {
            int anneeAutre = autre.getDateSouhaitee() != null ? autre.getDateSouhaitee().getYear() : LocalDate.now().getYear();
            if (anneeAutre == annee) {
                engage = engage.add(coutAnnuel(autre.getSalairePropose(), autre.getNombrePostes()));
            }
        }

        if ("REMPLACEMENT".equals(d.getMotif())) {
            return new ControleBudget(ControleBudget.ALLEGE, annee, budget.getMontantAlloue(), engage, BigDecimal.ZERO);
        }
        if (salaire == null) {
            return new ControleBudget(ControleBudget.NON_SAISI, annee, budget.getMontantAlloue(), engage, null);
        }
        BigDecimal cout = coutAnnuel(salaire, d.getNombrePostes());
        String statut = cout.compareTo(budget.getMontantAlloue().subtract(engage)) > 0
                ? ControleBudget.DEPASSE : ControleBudget.CONFORME;
        return new ControleBudget(statut, annee, budget.getMontantAlloue(), engage, cout);
    }

    private static BigDecimal coutAnnuel(BigDecimal salaire, Integer nombrePostes) {
        if (salaire == null) {
            return BigDecimal.ZERO;
        }
        int postes = nombrePostes == null || nombrePostes < 1 ? 1 : nombrePostes;
        return salaire.multiply(BigDecimal.valueOf(MOIS_PAR_AN)).multiply(BigDecimal.valueOf(postes));
    }

    private void appliquerCorrections(DemandeBesoinModel d, VerificationFinanceForm f, EmployeModel finance) {
        if (!memeMontant(d.getSalairePropose(), f.getSalairePropose())) {
            String remarque = "Salaire corrigé par la Finance : " + ControleSalaire.formater(d.getSalairePropose())
                    + " → " + ControleSalaire.formater(f.getSalairePropose());
            historiser(d, finance, remarque);
            // Le RH est informé : le salaire doit rester dans la grille.
            for (EmployeModel rh : employes.validateursParRole(ETAPE_RH)) {
                notifier(rh.getId(), d, "Salaire corrigé par la Finance sur la demande " + d.getNumero() + " ("
                        + ControleSalaire.formater(d.getSalairePropose()) + " → "
                        + ControleSalaire.formater(f.getSalairePropose()) + ")");
            }
            d.setSalairePropose(f.getSalairePropose());
        }
        if (d.getTypeContrat() != null && d.getTypeContrat().isAvecDuree()
                && !Objects.equals(d.getDureeMois(), f.getDureeMois())) {
            historiser(d, finance, "Durée du contrat corrigée par la Finance : " + d.getDureeMois() + " → "
                    + f.getDureeMois() + " mois");
            d.setDureeMois(f.getDureeMois());
        }
    }

    private DemandeBesoinModel chargerEnAttente(Integer id) {
        DemandeBesoinModel d = demandes.findById(id).orElse(null);
        if (d == null || !estActive(d) || trouverEtapeEnAttente(id) == null) {
            throw new DemandeException("Cette demande n'est pas en attente de votre vérification.");
        }
        return d;
    }

    private ValidationEtapeModel etapeEnAttente(Integer id) {
        ValidationEtapeModel v = trouverEtapeEnAttente(id);
        if (v == null) {
            throw new DemandeException("Cette demande n'est pas en attente de votre vérification.");
        }
        return v;
    }

    private ValidationEtapeModel trouverEtapeEnAttente(Integer id) {
        ValidationEtapeModel trouvee = null;
        for (ValidationEtapeModel v : validations.findByIdDemandeBesoinOrderByIdAsc(id)) {
            if (ETAPE_FINANCE.equals(v.getEtape()) && v.getDecision() == null) {
                trouvee = v;
            }
        }
        return trouvee;
    }

    private static boolean estActive(DemandeBesoinModel d) {
        return "SOUMISE".equals(d.getStatut()) || "EN_VERIFICATION".equals(d.getStatut());
    }

    private void cloturer(ValidationEtapeModel etape, String decision, String commentaire, EmployeModel finance) {
        etape.setDecision(decision);
        etape.setCommentaire(commentaire);
        etape.setIdValidateur(finance.getId());
        etape.setDateDecision(LocalDateTime.now());
        validations.save(etape);
    }

    private static boolean memeMontant(BigDecimal a, BigDecimal b) {
        if (a == null || b == null) {
            return a == b;
        }
        return a.compareTo(b) == 0;
    }

    private static String nettoyer(String texte) {
        if (texte == null || texte.isBlank()) {
            return null;
        }
        return texte.trim();
    }

    private void historiser(DemandeBesoinModel d, EmployeModel auteur, String remarque) {
        HistoriqueModificationModel h = new HistoriqueModificationModel();
        h.setIdDemandeBesoin(d.getId());
        h.setIdAuteur(auteur.getId());
        h.setRemarque(remarque.length() > 150 ? remarque.substring(0, 150) : remarque);
        h.setDateModification(LocalDateTime.now());
        historiques.save(h);
    }

    private void notifier(Integer destinataire, DemandeBesoinModel d, String message) {
        NotificationModel n = new NotificationModel();
        n.setIdDestinataire(destinataire);
        n.setIdDemandeBesoin(d.getId());
        n.setMessages(message);
        n.setDateEnvoi(LocalDateTime.now());
        notifications.save(n);
    }

    private String nomEmploye(Integer idEmploye) {
        if (idEmploye == null) {
            return "";
        }
        EmployeModel e = employes.findById(idEmploye).orElse(null);
        return e == null ? "" : e.getNomComplet();
    }
}
