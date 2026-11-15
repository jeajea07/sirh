package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.event.recrutement.OffreCreeeEvent;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.repository.recrutement.OffreRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationNouvelleOffreListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationNouvelleOffreListener.class);
    private final OffreRepository offres;
    private final DestinatairesRhResolver destinataires;
    private final NotificationRecrutementService notifications;

    public NotificationNouvelleOffreListener(OffreRepository offres, DestinatairesRhResolver destinataires,
            NotificationRecrutementService notifications) {
        this.offres = offres;
        this.destinataires = destinataires;
        this.notifications = notifications;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    // Transaction propre : après commit, la transaction d'origine est terminée (sinon « No active transaction »).
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifierRh(OffreCreeeEvent event) {
        Offre offre = offres.findById(event.idOffre()).orElse(null);
        if (offre == null) return;
        List<Integer> rh = destinataires.trouverDestinataires();
        if (rh.isEmpty()) {
            log.warn("Aucun destinataire RH configuré : l'offre {} est créée sans notification interne", offre.getReference());
            return;
        }
        for (Integer idRh : rh) {
            try {
                if (offre.getStatut() == com.gestionrh.sirh.enums.recrutement.OffreStatut.PUBLIEE) {
                    notifications.notifierEmploye(idRh, "OFFRE_PUBLIEE", "Offre publiée automatiquement",
                            "L'offre " + offre.getReference() + " (" + offre.getTitre() + ") a été créée depuis la demande "
                                    + "validée par la DG et publiée sur le site vitrine jusqu'au " + offre.getDateLimite()
                                    + ". Vous pouvez la modifier, la suspendre ou la clôturer.",
                            offre.getIdOffre(), null);
                } else {
                    notifications.notifierEmploye(idRh, "OFFRE_A_VALIDER", "Nouvelle offre à valider",
                            ModeleNotificationRecrutement.nouvelleOffre(offre.getReference()), offre.getIdOffre(), null);
                }
            } catch (RuntimeException e) {
                log.warn("Impossible de créer la notification RH pour l'offre {}", offre.getReference(), e);
            }
        }
    }
}
