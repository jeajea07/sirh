package com.gestionrh.sirh.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.HistoriqueModificationModel;
import com.gestionrh.sirh.model.NotificationModel;
import com.gestionrh.sirh.model.ValidationEtapeModel;
import com.gestionrh.sirh.repository.DemandeBesoinRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.HistoriqueModificationRepository;
import com.gestionrh.sirh.repository.NotificationRepository;
import com.gestionrh.sirh.repository.ValidationEtapeRepository;
import com.gestionrh.sirh.service.dto.DemandeDgResume;
import com.gestionrh.sirh.service.dto.HistoriqueRhLigne;

/** Décision finale de la Direction générale. */
@Service
public class DgService {

    public static final String ACTION_VALIDER = "valider";
    public static final String ACTION_RENVOYER = "renvoyer";
    public static final String ACTION_REJETER = "rejeter";
    private static final String ETAPE = "DG";
    private static final DateTimeFormatter JOUR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter JOUR_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DemandeBesoinRepository demandes;
    private final ValidationEtapeRepository validations;
    private final HistoriqueModificationRepository historiques;
    private final NotificationRepository notifications;
    private final EmployeRepository employes;

    public DgService(DemandeBesoinRepository demandes, ValidationEtapeRepository validations,
                     HistoriqueModificationRepository historiques, NotificationRepository notifications,
                     EmployeRepository employes) {
        this.demandes = demandes;
        this.validations = validations;
        this.historiques = historiques;
        this.notifications = notifications;
        this.employes = employes;
    }

    @Transactional(readOnly = true)
    public List<DemandeDgResume> aValider() {
        List<DemandeDgResume> resultat = new ArrayList<>();
        for (ValidationEtapeModel v : validations.findByEtapeAndDecisionIsNullOrderByDateReceptionAsc(ETAPE)) {
            demandes.findById(v.getIdDemandeBesoin()).filter(DgService::estActive).ifPresent(d -> {
                String demandeur = employes.findById(d.getIdDemandeur()).map(EmployeModel::getNomComplet).orElse("");
                resultat.add(new DemandeDgResume(d, demandeur));
            });
        }
        return resultat;
    }

    @Transactional(readOnly = true)
    public DemandeBesoinModel demandeEnAttente(Integer id) {
        return chargerEnAttente(id);
    }

    @Transactional(readOnly = true)
    public String dateReception(Integer id) {
        return etapeEnAttente(id).getDateReception().format(JOUR);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueRhLigne> historique(Integer idDg) {
        List<HistoriqueRhLigne> resultat = new ArrayList<>();
        for (ValidationEtapeModel v : validations.findByEtapeAndIdValidateurAndDecisionIsNotNullOrderByDateDecisionDesc(ETAPE, idDg)) {
            DemandeBesoinModel d = demandes.findById(v.getIdDemandeBesoin()).orElse(null);
            if (d == null) continue;
            String demandeur = employes.findById(d.getIdDemandeur()).map(EmployeModel::getNomComplet).orElse("");
            resultat.add(new HistoriqueRhLigne(d.getId(), d.getNumero(),
                    d.getPoste() == null ? null : d.getPoste().getDesignation(), d.getDepartement().getDesignation(),
                    demandeur, d.getTypeContrat() == null ? "-" : d.getTypeContrat().getCode(), d.getSalaireFr(),
                    v.getDecision(), v.getCommentaire(), v.getDateDecision() == null ? "" : v.getDateDecision().format(JOUR_HEURE),
                    d.getStatutLibelle(), d.getStatutBadge()));
        }
        return resultat;
    }

    @Transactional(readOnly = true)
    public List<String> verifier(Integer id, String action, String commentaire) {
        chargerEnAttente(id);
        List<String> erreurs = new ArrayList<>();
        if (!ACTION_VALIDER.equals(action) && !ACTION_RENVOYER.equals(action) && !ACTION_REJETER.equals(action)) {
            erreurs.add("Action inconnue.");
        } else if (!ACTION_VALIDER.equals(action) && (commentaire == null || commentaire.isBlank())) {
            erreurs.add(ACTION_RENVOYER.equals(action)
                    ? "Un commentaire est obligatoire pour renvoyer la demande au demandeur."
                    : "Un motif est obligatoire pour rejeter la demande.");
        }
        return erreurs;
    }

    @Transactional
    public String decider(Integer id, String action, String commentaire, EmployeModel dg) {
        DemandeBesoinModel d = chargerEnAttente(id);
        ValidationEtapeModel etape = etapeEnAttente(id);
        String note = commentaire == null || commentaire.isBlank() ? null : commentaire.trim();
        String decision;
        String statut;
        if (ACTION_VALIDER.equals(action)) {
            decision = "VALIDEE";
            statut = "VALIDEE";
        } else if (ACTION_RENVOYER.equals(action)) {
            decision = "RENVOYEE";
            statut = "A_CORRIGER";
        } else if (ACTION_REJETER.equals(action)) {
            decision = "REJETEE";
            statut = "REJETEE";
        } else {
            throw new DemandeException("Action inconnue.");
        }

        LocalDateTime maintenant = LocalDateTime.now();
        etape.setDecision(decision);
        etape.setCommentaire(note);
        etape.setIdValidateur(dg.getId());
        etape.setDateDecision(maintenant);
        validations.save(etape);
        d.setStatut(statut);
        demandes.save(d);

        String actionTexte = ACTION_VALIDER.equals(action) ? "validée définitivement"
                : ACTION_RENVOYER.equals(action) ? "renvoyée pour correction" : "rejetée";
        String message = "Votre demande " + d.getNumero() + " a été " + actionTexte + " par la Direction générale."
                + (note == null ? "" : " Commentaire : " + note);
        notifier(d.getIdDemandeur(), d, message);

        HistoriqueModificationModel h = new HistoriqueModificationModel();
        h.setIdDemandeBesoin(d.getId());
        h.setIdAuteur(dg.getId());
        h.setRemarque(("Décision finale DG : " + actionTexte + (note == null ? "" : " — " + note)));
        if (h.getRemarque().length() > 150) h.setRemarque(h.getRemarque().substring(0, 150));
        h.setDateModification(maintenant);
        historiques.save(h);

        if (ACTION_VALIDER.equals(action)) return "La demande " + d.getNumero() + " a été validée définitivement.";
        if (ACTION_RENVOYER.equals(action)) return "La demande " + d.getNumero() + " a été renvoyée au demandeur pour correction.";
        return "La demande " + d.getNumero() + " a été rejetée par la Direction générale.";
    }

    private DemandeBesoinModel chargerEnAttente(Integer id) {
        DemandeBesoinModel d = demandes.findById(id).orElse(null);
        if (d == null || !estActive(d) || trouverEtapeEnAttente(id) == null) {
            throw new DemandeException("Cette demande n'est pas en attente de la validation de la Direction générale.");
        }
        return d;
    }

    private ValidationEtapeModel etapeEnAttente(Integer id) {
        ValidationEtapeModel v = trouverEtapeEnAttente(id);
        if (v == null) throw new DemandeException("Cette demande n'est pas en attente de la validation de la Direction générale.");
        return v;
    }

    private ValidationEtapeModel trouverEtapeEnAttente(Integer id) {
        ValidationEtapeModel trouvee = null;
        for (ValidationEtapeModel v : validations.findByIdDemandeBesoinOrderByIdAsc(id)) {
            if (ETAPE.equals(v.getEtape()) && v.getDecision() == null) trouvee = v;
        }
        return trouvee;
    }

    private static boolean estActive(DemandeBesoinModel d) {
        return "SOUMISE".equals(d.getStatut()) || "EN_VERIFICATION".equals(d.getStatut());
    }

    private void notifier(Integer destinataire, DemandeBesoinModel d, String message) {
        NotificationModel n = new NotificationModel();
        n.setIdDestinataire(destinataire);
        n.setIdDemandeBesoin(d.getId());
        n.setMessages(message);
        n.setDateEnvoi(LocalDateTime.now());
        notifications.save(n);
    }
}
