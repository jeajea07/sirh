package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.OffreStatut;
import com.gestionrh.sirh.event.recrutement.OffreAnnuleeEvent;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.model.recrutement.HistoriqueCandidature;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.HistoriqueCandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.OffreRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class AnnulationOffreService {

    private static final Logger log = LoggerFactory.getLogger(AnnulationOffreService.class);
    private static final List<CandidatureStatut> STATUTS_EN_COURS = List.of(
            CandidatureStatut.RECUE, CandidatureStatut.RECU, CandidatureStatut.A_ETUDIER, CandidatureStatut.DOSSIER_VALIDE,
            CandidatureStatut.TEST_TECHNIQUE, CandidatureStatut.TEST_EN_COURS,
            CandidatureStatut.TEST_OK, CandidatureStatut.EN_ATTENTE, CandidatureStatut.ADMIS,
            CandidatureStatut.ENTRETIEN_RH,
            CandidatureStatut.ENTRETIEN_TECHNICIEN, CandidatureStatut.ENTRETIEN_DG,
            CandidatureStatut.CONFIRMATION_FINALE, CandidatureStatut.LISTE_ATTENTE);

    private final OffreRepository offres;
    private final CandidatureRepository candidatures;
    private final HistoriqueCandidatureRepository historiques;
    private final NotificationRecrutementService notifications;
    private final DestinatairesRhResolver destinatairesRh;

    public AnnulationOffreService(OffreRepository offres, CandidatureRepository candidatures,
            HistoriqueCandidatureRepository historiques, NotificationRecrutementService notifications,
            DestinatairesRhResolver destinatairesRh) {
        this.offres = offres;
        this.candidatures = candidatures;
        this.historiques = historiques;
        this.notifications = notifications;
        this.destinatairesRh = destinatairesRh;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void traiter(OffreAnnuleeEvent event) {
        Offre offre = offres.findById(event.idOffre()).orElse(null);
        if (offre == null || offre.getStatut() != OffreStatut.ANNULEE) return;
        for (Candidature candidature : candidatures.findByOffre_IdOffreAndStatutIn(offre.getIdOffre(), STATUTS_EN_COURS)) {
            CandidatureStatut ancien = candidature.getStatut();
            candidature.setStatut(CandidatureStatut.NON_RETENUE);
            candidatures.save(candidature);
            HistoriqueCandidature historique = new HistoriqueCandidature();
            historique.setCandidature(candidature);
            historique.setAction("ANNULATION_OFFRE");
            historique.setAncienStatut(ancien);
            historique.setNouveauStatut(CandidatureStatut.NON_RETENUE);
            historique.setCommentaire("Candidature clôturée à la suite de l'annulation de l'offre " + offre.getReference());
            historiques.save(historique);
            try {
                notifications.notifierCandidat(candidature.getCandidat().getIdCandidat(), "OFFRE_ANNULEE",
                        "Offre annulée", ModeleNotificationRecrutement.offreFermee(candidature.getNumero()),
                        offre.getIdOffre(), candidature.getIdCandidature());
            } catch (RuntimeException e) {
                log.warn("Notification d'annulation impossible pour la candidature {}", candidature.getNumero(), e);
            }
        }
        for (Integer idRh : destinatairesRh.trouverDestinataires()) {
            try {
                notifications.notifierEmploye(idRh, "OFFRE_ANNULEE", "Offre annulée",
                        "L'offre " + offre.getReference() + " a été annulée.", offre.getIdOffre(), null);
            } catch (RuntimeException e) {
                log.warn("Notification d'annulation impossible pour l'offre {}", offre.getReference(), e);
            }
        }
    }
}
