package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.dto.DemandeResumeDTO;
import com.gestionrh.sirh.model.CircuitValidationModel;
import com.gestionrh.sirh.model.CritereRecrutementModel;
import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.HistoriqueModificationModel;
import com.gestionrh.sirh.model.NotificationModel;
import com.gestionrh.sirh.model.PosteModel;
import com.gestionrh.sirh.model.TypeContratModel;
import com.gestionrh.sirh.model.ValidationEtapeModel;
import com.gestionrh.sirh.repository.CircuitValidationRepository;
import com.gestionrh.sirh.repository.CritereRecrutementRepository;
import com.gestionrh.sirh.repository.DemandeBesoinRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.HistoriqueModificationRepository;
import com.gestionrh.sirh.repository.NotificationRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.repository.TypeContratRepository;
import com.gestionrh.sirh.repository.ValidationEtapeRepository;

@Service
public class DemandeService {

    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final DemandeBesoinRepository demandes;
    private final CritereRecrutementRepository criteres;
    private final ValidationEtapeRepository validations;
    private final CircuitValidationRepository circuit;
    private final HistoriqueModificationRepository historiques;
    private final NotificationRepository notifications;
    private final EmployeRepository employes;
    private final PosteRepository postes;
    private final TypeContratRepository typesContrat;

    public DemandeService(DemandeBesoinRepository demandes, CritereRecrutementRepository criteres,
                          ValidationEtapeRepository validations, CircuitValidationRepository circuit,
                          HistoriqueModificationRepository historiques, NotificationRepository notifications,
                          EmployeRepository employes, PosteRepository postes, TypeContratRepository typesContrat) {
        this.demandes = demandes;
        this.criteres = criteres;
        this.validations = validations;
        this.circuit = circuit;
        this.historiques = historiques;
        this.notifications = notifications;
        this.employes = employes;
        this.postes = postes;
        this.typesContrat = typesContrat;
    }

    @Transactional(readOnly = true)
    public List<DemandeResumeDTO> lister(EmployeModel moi) {
        List<DemandeBesoinModel> liste = demandes.findByIdDemandeurOrderByIdDesc(moi.getId());
        List<DemandeResumeDTO> resultat = new ArrayList<>();
        for (DemandeBesoinModel d : liste) {
            completerDates(d);
            String poste = d.getPoste() == null ? null : d.getPoste().getDesignation();
            resultat.add(new DemandeResumeDTO(d.getId(), d.getNumero(), poste, d.getMotifLibelle(),
                    d.getDateCreationFr(), d.getDateSouhaiteeFr(), d.getStatutLibelle(), d.getStatutBadge()));
        }
        return resultat;
    }

    @Transactional(readOnly = true)
    public DemandeForm formulaireDe(Integer id, EmployeModel moi) {
        DemandeBesoinModel d = charger(id, moi);
        if (!d.isModifiable()) {
            throw new DemandeException("Cette demande ne peut plus être modifiée.");
        }
        return versFormulaire(d);
    }

    public List<String> verifierBrouillon(DemandeForm f) {
        List<String> erreurs = new ArrayList<>();

        if (f.getIdPoste() == null || !postes.existsById(f.getIdPoste())) {
            erreurs.add("Le poste à pourvoir est obligatoire.");
        }
        if (f.getNombrePostes() == null || f.getNombrePostes() < 1) {
            erreurs.add("Le nombre de postes doit être au moins 1.");
        }
        if (!estDansListe(f.getMotif(), "REMPLACEMENT", "RENFORT", "CREATION")) {
            erreurs.add("Le motif de la demande est obligatoire.");
        }
        if (f.getIdTypeContrat() == null || !typesContrat.existsById(f.getIdTypeContrat())) {
            erreurs.add("Le type de contrat est obligatoire.");
        }
        if (f.getSalairePropose() == null || f.getSalairePropose().signum() <= 0) {
            erreurs.add("Le salaire proposé doit être supérieur à 0.");
        }
        if (f.getDateSouhaitee() == null) {
            erreurs.add("La date souhaitée est obligatoire.");
        }
        return erreurs;
    }

    @Transactional(readOnly = true)
    public List<String> verifierSoumission(DemandeForm f) {
        List<String> erreurs = verifierBrouillon(f);

        if (!estDansListe(f.getNiveauUrgence(), "URGENT", "CRITIQUE")) {
            erreurs.add("Le niveau d'urgence est obligatoire.");
        }

        TypeContratModel type = null;
        if (f.getIdTypeContrat() != null) {
            type = typesContrat.findById(f.getIdTypeContrat()).orElse(null);
        }
        if (type != null && type.isAvecDuree() && (f.getDureeMois() == null || f.getDureeMois() < 1)) {
            erreurs.add("La durée du contrat (en mois) est obligatoire pour un " + type.getCode() + ".");
        }

        if (f.getDateSouhaitee() != null && f.getDateSouhaitee().isBefore(LocalDate.now())) {
            erreurs.add("La date souhaitée ne peut pas être dans le passé.");
        }

        if (f.getJustification() == null || f.getJustification().isBlank()) {
            erreurs.add("La justification est obligatoire.");
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

        if (f.getIdPoste() != null && f.getDepartementId() != null) {
            Integer idActuel = f.getId() == null ? 0 : f.getId();
            List<DemandeBesoinModel> similaires = demandes.demandesSimilairesEnCours(f.getDepartementId(), f.getIdPoste(), idActuel);
            if (!similaires.isEmpty()) {
                erreurs.add("Une demande similaire est déjà en cours pour ce poste dans votre département : "
                        + similaires.get(0).getNumero() + ".");
            }
        }
        return erreurs;
    }

    @Transactional
    public DemandeBesoinModel enregistrer(DemandeForm f, EmployeModel moi) {
        boolean nouvelle = f.getId() == null;
        DemandeBesoinModel d;
        if (nouvelle) {
            d = new DemandeBesoinModel();
            d.setNumero(genererNumero());
            d.setIdDemandeur(moi.getId());
            d.setDepartement(moi.getDepartement());
            d.setStatut("BROUILLON");
        } else {
            d = charger(f.getId(), moi);
            if (!d.isModifiable()) {
                throw new DemandeException("Cette demande ne peut plus être modifiée.");
            }
        }

        PosteModel poste = null;
        if (f.getIdPoste() != null) {
            poste = postes.findById(f.getIdPoste()).orElse(null);
        }
        TypeContratModel type = null;
        if (f.getIdTypeContrat() != null) {
            type = typesContrat.findById(f.getIdTypeContrat()).orElse(null);
        }

        d.setPoste(poste);
        if (f.getNombrePostes() == null || f.getNombrePostes() < 1) {
            d.setNombrePostes(1);
        } else {
            d.setNombrePostes(f.getNombrePostes());
        }
        d.setMotif(f.getMotif());
        d.setNiveauUrgence(f.getNiveauUrgence());
        d.setTypeContrat(type);
        if (type != null && type.isAvecDuree()) {
            d.setDureeMois(f.getDureeMois());
        } else {
            d.setDureeMois(null);
        }
        d.setSalairePropose(f.getSalairePropose());
        d.setDateSouhaitee(f.getDateSouhaitee());
        d.setJustification(f.getJustification());

        CritereRecrutementModel critere = d.getCritereRecrutement();
        boolean critereRempli = f.getNiveauEtude() != null || f.getAnneeExperience() != null
                || f.getCompetence() != null || f.getMissions() != null;
        if (critereRempli) {
            if (critere == null) {
                critere = new CritereRecrutementModel();
            }
            critere.setNiveauEtude(f.getNiveauEtude());
            critere.setAnneeExperience(f.getAnneeExperience());
            critere.setCompetence(f.getCompetence());
            critere.setMissions(f.getMissions());
            critere = criteres.save(critere);
            d.setCritereRecrutement(critere);
        } else {
            d.setCritereRecrutement(null);
        }

        d = demandes.save(d);

        if (nouvelle) {
            historiser(d, moi, "Brouillon créé");
        }
        return d;
    }

    @Transactional
    public List<String> soumettre(Integer id, EmployeModel moi) {
        DemandeBesoinModel d = charger(id, moi);
        if (!d.isModifiable()) {
            throw new DemandeException("Cette demande a déjà été soumise.");
        }
        List<String> erreurs = verifierSoumission(versFormulaire(d));
        if (!erreurs.isEmpty()) {
            throw new DemandeException(erreurs.get(0));
        }

        boolean correction = "A_CORRIGER".equals(d.getStatut());

        String etape = "RH";
        if (correction) {
            List<ValidationEtapeModel> lignes = validations.findByIdDemandeBesoinOrderByIdAsc(id);
            for (int i = lignes.size() - 1; i >= 0; i--) {
                if ("RENVOYEE".equals(lignes.get(i).getDecision())) {
                    etape = lignes.get(i).getEtape();
                    break;
                }
            }
        }

        d.setStatut("SOUMISE");
        demandes.save(d);

        ValidationEtapeModel v = new ValidationEtapeModel();
        v.setIdDemandeBesoin(id);
        v.setEtape(etape);
        v.setDateReception(LocalDateTime.now());
        validations.save(v);

        if (correction) {
            historiser(d, moi, "Demande corrigée et soumise de nouveau");
        } else {
            historiser(d, moi, "Demande soumise");
        }
        notifier(d, moi, etape);

        return new ArrayList<>();
    }

    @Transactional
    public void retirer(Integer id, EmployeModel moi) {
        DemandeBesoinModel d = charger(id, moi);
        if (!d.isRetirable()) {
            throw new DemandeException("Cette demande ne peut plus être retirée.");
        }
        d.setStatut("RETIREE");
        demandes.save(d);
        historiser(d, moi, "Demande retirée par le demandeur");
    }

    @Transactional(readOnly = true)
    public SuiviDemande suivre(Integer id, EmployeModel moi) {
        DemandeBesoinModel d = charger(id, moi);

        List<ValidationEtapeModel> lignes = validations.findByIdDemandeBesoinOrderByIdAsc(id);
        List<CircuitValidationModel> etapesCircuit = circuit.findAllByOrderByIdAsc();
        boolean brouillon = "BROUILLON".equals(d.getStatut());
        boolean retiree = "RETIREE".equals(d.getStatut());

        List<SuiviDemande.Etape> etapes = new ArrayList<>();
        String premierEtat = brouillon ? "current" : "done";
        String premierSymbole = brouillon ? "1" : "✓";
        String premiereInfo = brouillon ? "Brouillon" : d.getDateSoumissionFr();
        etapes.add(new SuiviDemande.Etape(premierSymbole, "Demande soumise", premierEtat, premiereInfo));

        String enCours = null;
        int numero = 2;
        for (CircuitValidationModel c : etapesCircuit) {
            ValidationEtapeModel derniere = null;
            for (ValidationEtapeModel v : lignes) {
                if (c.getEtape().equals(v.getEtape())) {
                    derniere = v;
                }
            }

            String etat = "todo";
            String symbole = String.valueOf(numero);
            String info = "";

            if (derniere == null) {
                if ("RESPONSABLE".equals(c.getEtape())) {

                    continue;
                }
            } else if (derniere.getDecision() == null) {
                if (!retiree) {
                    etat = "current";
                    info = "En attente";
                    LocalDate limite = ajouterJoursOuvrables(derniere.getDateReception().toLocalDate(), c.getDelaiJours());
                    enCours = phraseEtape(c.getEtape()) + " depuis le " + derniere.getDateReception().format(JOUR)
                            + ". Réponse attendue avant le " + limite.format(JOUR) + ".";
                }
            } else if ("VALIDEE".equals(derniere.getDecision())) {
                etat = "done";
                symbole = "✓";
                if (derniere.getDateDecision() != null) {
                    info = derniere.getDateDecision().format(JOUR);
                }
            } else if ("RENVOYEE".equals(derniere.getDecision())) {
                etat = "current";
                info = "À corriger";
            } else {
                etat = "bad";
                symbole = "✕";
                info = "Rejetée";
            }
            etapes.add(new SuiviDemande.Etape(symbole, libelleCourt(c.getEtape()), etat, info));
            numero++;
        }

        List<SuiviDemande.Evenement> evenements = new ArrayList<>();
        for (HistoriqueModificationModel h : historiques.findByIdDemandeBesoinOrderByIdAsc(id)) {
            String auteur = nomEmploye(h.getIdAuteur());
            evenements.add(new SuiviDemande.Evenement(h.getDateModification(),
                    formaterDateHeure(h.getDateModification()), h.getRemarque(), auteur));
        }
        String commentaireDecision = null;
        for (ValidationEtapeModel v : lignes) {
            if (v.getDecision() == null || v.getDateDecision() == null) {
                continue;
            }
            String titre;
            if ("VALIDEE".equals(v.getDecision())) {
                titre = "Validée : " + libelleCourt(v.getEtape());
            } else if ("RENVOYEE".equals(v.getDecision())) {
                titre = "Renvoyée pour correction : " + libelleCourt(v.getEtape());
            } else {
                titre = "Rejetée : " + libelleCourt(v.getEtape());
            }
            String detail = nomEmploye(v.getIdValidateur());
            if (v.getCommentaire() != null && !v.getCommentaire().isBlank()) {
                if (detail.isEmpty()) {
                    detail = v.getCommentaire();
                } else {
                    detail = detail + " — " + v.getCommentaire();
                }
                if (!"VALIDEE".equals(v.getDecision())) {
                    commentaireDecision = v.getCommentaire();
                }
            }
            evenements.add(new SuiviDemande.Evenement(v.getDateDecision(),
                    formaterDateHeure(v.getDateDecision()), titre, detail));
        }
        trierParDateDecroissante(evenements);

        return new SuiviDemande(d, etapes, evenements, enCours, commentaireDecision);
    }

    private DemandeBesoinModel charger(Integer id, EmployeModel moi) {
        DemandeBesoinModel d = demandes.findById(id).orElse(null);
        if (d == null) {
            throw new DemandeException("Cette demande est introuvable.");
        }
        if (!d.getIdDemandeur().equals(moi.getId())) {
            throw new DemandeException("Cette demande est introuvable.");
        }
        completerDates(d);
        return d;
    }

    private void completerDates(DemandeBesoinModel d) {
        HistoriqueModificationModel creation = historiques.findFirstByIdDemandeBesoinOrderByIdAsc(d.getId()).orElse(null);
        if (creation != null) {
            d.setDateCreation(creation.getDateModification());
        }
        HistoriqueModificationModel soumission = historiques
                .findFirstByIdDemandeBesoinAndRemarqueContainingOrderByIdDesc(d.getId(), "soumise").orElse(null);
        if (soumission != null) {
            d.setDateSoumission(soumission.getDateModification());
        }
    }

    private DemandeForm versFormulaire(DemandeBesoinModel d) {
        DemandeForm f = new DemandeForm();
        f.setId(d.getId());
        if (d.getPoste() != null) {
            f.setIdPoste(d.getPoste().getId());
        }
        f.setNombrePostes(d.getNombrePostes());
        f.setMotif(d.getMotif());
        f.setNiveauUrgence(d.getNiveauUrgence());
        if (d.getTypeContrat() != null) {
            f.setIdTypeContrat(d.getTypeContrat().getId());
        }
        f.setDureeMois(d.getDureeMois());
        f.setSalairePropose(d.getSalairePropose());
        f.setDateSouhaitee(d.getDateSouhaitee());
        f.setJustification(d.getJustification());
        f.setDepartementId(d.getDepartement().getId());

        CritereRecrutementModel c = d.getCritereRecrutement();
        if (c != null) {
            f.setNiveauEtude(c.getNiveauEtude());
            f.setAnneeExperience(c.getAnneeExperience());
            f.setCompetence(c.getCompetence());
            f.setMissions(c.getMissions());
        }
        return f;
    }

    private String genererNumero() {
        int numero = demandes.prochainNumero();
        int annee = LocalDate.now().getYear();
        return String.format("DB-%d-%04d", annee, numero);
    }

    private void historiser(DemandeBesoinModel d, EmployeModel auteur, String remarque) {
        HistoriqueModificationModel h = new HistoriqueModificationModel();
        h.setIdDemandeBesoin(d.getId());
        h.setIdAuteur(auteur.getId());
        h.setRemarque(remarque);
        h.setDateModification(LocalDateTime.now());
        historiques.save(h);
    }

    private void notifier(DemandeBesoinModel d, EmployeModel auteur, String etape) {
        List<EmployeModel> destinataires;
        if ("RESPONSABLE".equals(etape)) {
            destinataires = employes.validateursDuDepartement("RESPONSABLE", d.getDepartement().getId());
        } else {
            destinataires = employes.validateursParRole(etape);
        }
        String message = "Nouvelle demande à traiter : " + d.getNumero();
        if (d.getPoste() != null) {
            message = message + " — " + d.getPoste().getDesignation();
        }
        message = message + " (" + d.getDepartement().getDesignation() + ")";

        for (EmployeModel e : destinataires) {
            if (e.getId().equals(auteur.getId())) {
                continue;
            }
            NotificationModel n = new NotificationModel();
            n.setIdDestinataire(e.getId());
            n.setIdDemandeBesoin(d.getId());
            n.setMessages(message);
            n.setDateEnvoi(LocalDateTime.now());
            notifications.save(n);
        }
    }

    private String nomEmploye(Integer idEmploye) {
        if (idEmploye == null) {
            return "";
        }
        EmployeModel e = employes.findById(idEmploye).orElse(null);
        if (e == null) {
            return "";
        }
        return e.getNomComplet();
    }

    private String formaterDateHeure(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private static boolean existeEtape(List<ValidationEtapeModel> lignes, String etape) {
        for (ValidationEtapeModel v : lignes) {
            if (etape.equals(v.getEtape())) {
                return true;
            }
        }
        return false;
    }

    private static boolean estDansListe(String valeur, String... valeursPermises) {
        if (valeur == null) {
            return false;
        }
        for (String v : valeursPermises) {
            if (v.equals(valeur)) {
                return true;
            }
        }
        return false;
    }

    private static void trierParDateDecroissante(List<SuiviDemande.Evenement> evenements) {
        for (int i = 0; i < evenements.size(); i++) {
            for (int j = i + 1; j < evenements.size(); j++) {
                if (evenements.get(j).getDate().isAfter(evenements.get(i).getDate())) {
                    SuiviDemande.Evenement temp = evenements.get(i);
                    evenements.set(i, evenements.get(j));
                    evenements.set(j, temp);
                }
            }
        }
    }

    private static LocalDate ajouterJoursOuvrables(LocalDate depart, int jours) {
        LocalDate date = depart;
        int restants = jours;
        while (restants > 0) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                restants--;
            }
        }
        return date;
    }

    private static String libelleCourt(String etape) {
        if ("RESPONSABLE".equals(etape)) {
            return "Responsable";
        }
        if ("RH".equals(etape)) {
            return "RH";
        }
        if ("FINANCE".equals(etape)) {
            return "Finance";
        }
        if ("DG".equals(etape)) {
            return "DG";
        }
        return etape;
    }

    private static String phraseEtape(String etape) {
        if ("RESPONSABLE".equals(etape)) {
            return "En attente de la validation du responsable de votre département";
        }
        if ("RH".equals(etape)) {
            return "En attente de la vérification par le RH";
        }
        if ("FINANCE".equals(etape)) {
            return "En attente de la vérification par la Finance";
        }
        if ("DG".equals(etape)) {
            return "En attente de la décision de la Direction générale";
        }
        return "En attente : " + etape;
    }
}
