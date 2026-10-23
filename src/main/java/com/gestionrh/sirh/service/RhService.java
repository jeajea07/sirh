package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.CritereRecrutementModel;
import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.model.DepartementModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.GrilleSalarialeModel;
import com.gestionrh.sirh.model.HistoriqueModificationModel;
import com.gestionrh.sirh.model.NotificationModel;
import com.gestionrh.sirh.model.PosteModel;
import com.gestionrh.sirh.model.TypeContratModel;
import com.gestionrh.sirh.model.ValidationEtapeModel;
import com.gestionrh.sirh.repository.CritereRecrutementRepository;
import com.gestionrh.sirh.repository.DemandeBesoinRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.GrilleSalarialeRepository;
import com.gestionrh.sirh.repository.HistoriqueModificationRepository;
import com.gestionrh.sirh.repository.NotificationRepository;
import com.gestionrh.sirh.repository.ValidationEtapeRepository;
import com.gestionrh.sirh.service.dto.ControleSalaire;
import com.gestionrh.sirh.service.dto.DemandeRhResume;
import com.gestionrh.sirh.service.dto.HistoriqueRhLigne;

@Service
public class RhService {

    public static final String ACTION_CORRIGER = "corriger";
    public static final String ACTION_VALIDER = "valider";
    public static final String ACTION_RENVOYER = "renvoyer";
    public static final String ACTION_REJETER = "rejeter";

    private static final String ETAPE_RH = "RH";
    private static final String ETAPE_SUIVANTE = "FINANCE";
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter JOUR_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DemandeBesoinRepository demandes;
    private final ValidationEtapeRepository validations;
    private final HistoriqueModificationRepository historiques;
    private final NotificationRepository notifications;
    private final EmployeRepository employes;
    private final CritereRecrutementRepository criteres;
    private final GrilleSalarialeRepository grilles;

    public RhService(DemandeBesoinRepository demandes, ValidationEtapeRepository validations,
                     HistoriqueModificationRepository historiques, NotificationRepository notifications,
                     EmployeRepository employes, CritereRecrutementRepository criteres,
                     GrilleSalarialeRepository grilles) {
        this.demandes = demandes;
        this.validations = validations;
        this.historiques = historiques;
        this.notifications = notifications;
        this.employes = employes;
        this.criteres = criteres;
        this.grilles = grilles;
    }

    @Transactional(readOnly = true)
    public List<DemandeRhResume> aVerifier() {
        List<DemandeRhResume> resultat = new ArrayList<>();
        for (ValidationEtapeModel v : validations.findByEtapeAndDecisionIsNullOrderByDateReceptionAsc(ETAPE_RH)) {
            DemandeBesoinModel d = demandes.findById(v.getIdDemandeBesoin()).orElse(null);
            if (d == null || !estActive(d)) {
                continue;
            }
            ControleSalaire controle = controlerSalaire(d.getPoste(), d.getDepartement(), d.getTypeContrat(), niveauDe(d), d.getSalairePropose());
            resultat.add(new DemandeRhResume(d.getId(), d.getNumero(),
                    d.getPoste() == null ? null : d.getPoste().getDesignation(),
                    d.getDepartement().getDesignation(), nomEmploye(d.getIdDemandeur()),
                    d.getTypeContrat() == null ? "-" : d.getTypeContrat().getCode(),
                    d.getUrgenceLibelle(), v.getDateReception().format(JOUR), controle));
        }
        return resultat;
    }

    /** Les décisions prises à l'étape RH, la plus récente d'abord. {@code idRh} null = toutes (vue du DG). */
    @Transactional(readOnly = true)
    public List<HistoriqueRhLigne> historique(Integer idRh) {
        List<HistoriqueRhLigne> resultat = new ArrayList<>();
        List<ValidationEtapeModel> decisions = idRh == null
                ? validations.findByEtapeAndDecisionIsNotNullOrderByDateDecisionDesc(ETAPE_RH)
                : validations.findByEtapeAndIdValidateurAndDecisionIsNotNullOrderByDateDecisionDesc(ETAPE_RH, idRh);
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
    public VerificationRhForm formulaireDe(Integer id) {
        return formulaireDe(chargerEnAttente(id));
    }

    @Transactional(readOnly = true)
    public VerificationRh preparer(Integer id, VerificationRhForm saisie) {
        DemandeBesoinModel d = chargerEnAttente(id);
        ValidationEtapeModel v = etapeEnAttente(id);
        VerificationRhForm f = saisie != null ? saisie : formulaireDe(d);

        ControleSalaire controle = controlerSalaire(d.getPoste(), d.getDepartement(), d.getTypeContrat(), f.getNiveauEtude(), f.getSalairePropose());

        List<String> alertes = new ArrayList<>();
        CritereRecrutementModel c = d.getCritereRecrutement();
        if (c == null || c.getNiveauEtude() == null || c.getAnneeExperience() == null
                || c.getCompetence() == null || c.getCompetence().isBlank()) {
            alertes.add("Les critères de recrutement ne sont pas tous renseignés.");
        }
        List<DemandeBesoinModel> similaires = demandes.demandesSimilairesEnCours(
                d.getDepartement().getId(), d.getPoste().getId(), d.getId());
        if (!similaires.isEmpty()) {
            alertes.add("Une demande similaire est déjà en cours pour ce poste dans ce département : "
                    + similaires.get(0).getNumero() + ".");
        }
        Integer effectifAutorise = d.getDepartement().getEffectif();
        if (effectifAutorise != null) {
            int actuel = employes.findByDepartement_IdAndActifTrueOrderByNomAsc(d.getDepartement().getId()).size();
            if (actuel >= effectifAutorise) {
                alertes.add("L'effectif autorisé du département (" + effectifAutorise + ") est déjà atteint ("
                        + actuel + " employés actifs).");
            }
        }

        boolean dureeRequise = d.getTypeContrat() != null && d.getTypeContrat().isAvecDuree();
        return new VerificationRh(d, nomEmploye(d.getIdDemandeur()), v.getDateReception().format(JOUR),
                controle, alertes, dureeRequise);
    }

    @Transactional(readOnly = true)
    public List<String> verifier(Integer id, String action, VerificationRhForm f) {
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
        if (!estDansListe(f.getNiveauEtude(), "LICENCE", "MASTER", "PHD")) {
            erreurs.add("Le niveau d'étude demandé est obligatoire.");
        }
        if (f.getAnneeExperience() == null || f.getAnneeExperience() < 0) {
            erreurs.add("Le nombre d'années d'expérience est obligatoire (0 si aucune).");
        }
        if (f.getCompetence() == null || f.getCompetence().isBlank()) {
            erreurs.add("Les compétences demandées sont obligatoires.");
        }

        // Un salaire au-dessus de la grille n'empêche pas la validation : l'alerte reste affichée à titre d'information.
        return erreurs;
    }

    @Transactional
    public String decider(Integer id, String action, VerificationRhForm f, EmployeModel rh) {
        DemandeBesoinModel d = chargerEnAttente(id);
        ValidationEtapeModel etape = etapeEnAttente(id);
        String commentaire = nettoyer(f.getCommentaire());

        if (ACTION_CORRIGER.equals(action)) {
            appliquerCorrections(d, f, rh);
            demandes.save(d);
            return "Corrections enregistrées.";
        }

        if (ACTION_VALIDER.equals(action)) {
            appliquerCorrections(d, f, rh);
            cloturer(etape, "VALIDEE", commentaire, rh);
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
            return "La demande " + d.getNumero() + " a été validée et envoyée à la Finance.";
        }

        if (ACTION_RENVOYER.equals(action)) {
            cloturer(etape, "RENVOYEE", commentaire, rh);
            d.setStatut("A_CORRIGER");
            demandes.save(d);
            notifier(d.getIdDemandeur(), d, "Votre demande " + d.getNumero()
                    + " a été renvoyée pour correction par la RH. Commentaire : " + commentaire);
            return "La demande " + d.getNumero() + " a été renvoyée au demandeur pour correction.";
        }

        if (ACTION_REJETER.equals(action)) {
            cloturer(etape, "REJETEE", commentaire, rh);
            d.setStatut("REJETEE");
            demandes.save(d);
            notifier(d.getIdDemandeur(), d, "Votre demande " + d.getNumero()
                    + " a été rejetée par la RH. Motif : " + commentaire);
            return "La demande " + d.getNumero() + " a été rejetée.";
        }
        throw new DemandeException("Action inconnue.");
    }

    public ControleSalaire controlerSalaire(PosteModel poste, DepartementModel departement, TypeContratModel typeContrat,
                                            String niveauEtude, BigDecimal salaire) {
        GrilleSalarialeModel ligne = trouverGrille(poste, departement, typeContrat, niveauEtude);
        BigDecimal plafond = ligne == null ? null : ligne.getSalaire();
        if (salaire == null) {
            return new ControleSalaire(ControleSalaire.NON_SAISI, null, plafond);
        }
        if (plafond == null) {
            return new ControleSalaire(ControleSalaire.SANS_GRILLE, salaire, null);
        }
        String statut = salaire.compareTo(plafond) > 0 ? ControleSalaire.DEPASSE : ControleSalaire.CONFORME;
        return new ControleSalaire(statut, salaire, plafond);
    }

    private GrilleSalarialeModel trouverGrille(PosteModel poste, DepartementModel departement, TypeContratModel typeContrat,
                                               String niveauEtude) {
        if (poste == null || departement == null || typeContrat == null) {
            return null;
        }
        GrilleSalarialeModel meilleure = null;
        int meilleurScore = -1;
        for (GrilleSalarialeModel g : grilles.candidats(poste.getId(), departement.getId(), typeContrat.getId())) {
            int score = 0;
            if (g.getDepartement() != null) {
                score += 2;
            }
            if (g.getNiveau() != null && !g.getNiveau().isBlank()) {
                if (niveauEtude != null && correspondNiveau(g.getNiveau(), niveauEtude)) {
                    score += 1;
                } else if (niveauEtude != null) {
                    continue;
                }
            }
            if (score > meilleurScore) {
                meilleurScore = score;
                meilleure = g;
            }
        }
        return meilleure;
    }

    private static boolean correspondNiveau(String niveauGrille, String niveauEtude) {
        String a = niveauGrille.trim().toLowerCase();
        String b = niveauEtude.trim().toLowerCase();
        if (a.equals(b)) {
            return true;
        }
        if (b.equals("licence")) { return a.startsWith("licence"); }
        if (b.equals("master")) { return a.startsWith("master"); }
        if (b.equals("phd")) { return a.startsWith("doctorat") || a.startsWith("phd"); }
        return false;
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
            if (ETAPE_RH.equals(v.getEtape()) && v.getDecision() == null) {
                trouvee = v;
            }
        }
        return trouvee;
    }

    private static boolean estActive(DemandeBesoinModel d) {
        return "SOUMISE".equals(d.getStatut()) || "EN_VERIFICATION".equals(d.getStatut());
    }

    private void cloturer(ValidationEtapeModel etape, String decision, String commentaire, EmployeModel rh) {
        etape.setDecision(decision);
        etape.setCommentaire(commentaire);
        etape.setIdValidateur(rh.getId());
        etape.setDateDecision(LocalDateTime.now());
        validations.save(etape);
    }

    private VerificationRhForm formulaireDe(DemandeBesoinModel d) {
        VerificationRhForm f = new VerificationRhForm();
        f.setSalairePropose(d.getSalairePropose());
        f.setDureeMois(d.getDureeMois());
        CritereRecrutementModel c = d.getCritereRecrutement();
        if (c != null) {
            f.setNiveauEtude(c.getNiveauEtude());
            f.setAnneeExperience(c.getAnneeExperience());
            f.setCompetence(c.getCompetence());
            f.setMissions(c.getMissions());
        }
        return f;
    }

    private static String niveauDe(DemandeBesoinModel d) {
        return d.getCritereRecrutement() == null ? null : d.getCritereRecrutement().getNiveauEtude();
    }

    private void appliquerCorrections(DemandeBesoinModel d, VerificationRhForm f, EmployeModel rh) {
        if (!memeMontant(d.getSalairePropose(), f.getSalairePropose())) {
            historiser(d, rh, "Salaire corrigé par la RH : " + ControleSalaire.formater(d.getSalairePropose())
                    + " → " + ControleSalaire.formater(f.getSalairePropose()));
            d.setSalairePropose(f.getSalairePropose());
        }
        if (d.getTypeContrat() != null && d.getTypeContrat().isAvecDuree()
                && !Objects.equals(d.getDureeMois(), f.getDureeMois())) {
            historiser(d, rh, "Durée du contrat corrigée par la RH : " + d.getDureeMois() + " → "
                    + f.getDureeMois() + " mois");
            d.setDureeMois(f.getDureeMois());
        }

        CritereRecrutementModel c = d.getCritereRecrutement();
        if (c == null) {
            c = new CritereRecrutementModel();
        }
        if (!Objects.equals(c.getNiveauEtude(), f.getNiveauEtude())) {
            historiser(d, rh, "Niveau d'étude corrigé par la RH : " + libelleNiveau(c.getNiveauEtude())
                    + " → " + libelleNiveau(f.getNiveauEtude()));
            c.setNiveauEtude(f.getNiveauEtude());
        }
        if (!Objects.equals(c.getAnneeExperience(), f.getAnneeExperience())) {
            historiser(d, rh, "Années d'expérience corrigées par la RH : " + c.getAnneeExperience()
                    + " → " + f.getAnneeExperience());
            c.setAnneeExperience(f.getAnneeExperience());
        }
        if (!Objects.equals(nettoyer(c.getCompetence()), nettoyer(f.getCompetence()))) {
            historiser(d, rh, "Compétences corrigées par la RH");
            c.setCompetence(nettoyer(f.getCompetence()));
        }
        if (!Objects.equals(nettoyer(c.getMissions()), nettoyer(f.getMissions()))) {
            historiser(d, rh, "Missions corrigées par la RH");
            c.setMissions(nettoyer(f.getMissions()));
        }
        d.setCritereRecrutement(criteres.save(c));
    }

    private static String libelleNiveau(String code) {
        if (code == null) { return "-"; }
        if (code.equals("LICENCE")) { return "Licence"; }
        if (code.equals("MASTER")) { return "Master"; }
        if (code.equals("PHD")) { return "Doctorat"; }
        return code;
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

    private static boolean estDansListe(String valeur, String... permises) {
        if (valeur == null) {
            return false;
        }
        for (String p : permises) {
            if (p.equals(valeur)) {
                return true;
            }
        }
        return false;
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
