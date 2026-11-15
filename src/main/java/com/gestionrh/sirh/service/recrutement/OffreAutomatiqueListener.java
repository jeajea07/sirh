package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.enums.recrutement.OffreStatut;
import com.gestionrh.sirh.event.recrutement.DemandeValideeEvent;
import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.repository.DemandeBesoinRepository;
import com.gestionrh.sirh.repository.recrutement.OffreRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Fusion bloc 1 → bloc 2 (Bloc 2, étape 1 : « Demande acceptée → Offre créée automatiquement → Validation
 * du RH → Publication ») : dès que la DG valide une demande, l'offre est créée en brouillon, avec une date
 * limite pré-remplie, et la RH est notifiée. La validation RH publie l'offre dans le même clic.
 *
 * - Date limite par défaut : aujourd'hui + app.recrutement.duree-publication-jours (30 jours).
 * - app.recrutement.publication-automatique=true : publication sans validation RH (écart au Bloc 2).
 * - Exécuté après la décision de la DG, dans une transaction séparée : un échec n'annule jamais la décision.
 * - Au démarrage, les demandes validées sans aucune offre (validées avant cette fonctionnalité) sont rattrapées.
 */
@Component
public class OffreAutomatiqueListener {

    private static final Logger log = LoggerFactory.getLogger(OffreAutomatiqueListener.class);

    private final OffreDepuisDemandeService offreDepuisDemande;
    private final OffreService offreService;
    private final OffreRepository offres;
    private final DemandeBesoinRepository demandes;
    private final TransactionTemplate transaction;
    private final boolean publicationAutomatique;
    private final int dureePublicationJours;

    public OffreAutomatiqueListener(OffreDepuisDemandeService offreDepuisDemande, OffreService offreService,
            OffreRepository offres, DemandeBesoinRepository demandes, TransactionTemplate transaction,
            @Value("${app.recrutement.publication-automatique:false}") boolean publicationAutomatique,
            @Value("${app.recrutement.duree-publication-jours:30}") int dureePublicationJours) {
        this.offreDepuisDemande = offreDepuisDemande;
        this.offreService = offreService;
        this.offres = offres;
        this.demandes = demandes;
        this.transaction = transaction;
        this.publicationAutomatique = publicationAutomatique;
        this.dureePublicationJours = dureePublicationJours;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void surDemandeValidee(DemandeValideeEvent event) {
        traiter(event.idDemandeBesoin(), event.idValidateur());
    }

    /** Rattrapage : demandes VALIDEE n'ayant jamais eu d'offre. */
    @EventListener(ApplicationReadyEvent.class)
    public void rattraperDemandesValidees() {
        List<Integer> enAttente = demandes.findAll().stream()
                .filter(d -> "VALIDEE".equals(d.getStatut()))
                .map(DemandeBesoinModel::getId)
                .filter(id -> !offres.existsByIdDemandeBesoin(id))
                .toList();
        for (Integer id : enAttente) {
            try {
                transaction.executeWithoutResult(statut -> traiter(id, null));
            } catch (RuntimeException e) {
                log.warn("Rattrapage de l'offre impossible pour la demande {} : {}", id, e.getMessage());
            }
        }
    }

    private void traiter(Integer idDemande, Integer idValidateur) {
        try {
            Offre offre = offreDepuisDemande.creerDepuisDemande(idDemande);
            if (offre.getStatut() != OffreStatut.BROUILLON) {
                return;
            }
            // Date limite pré-remplie : la RH n'a plus qu'à vérifier et valider (un clic).
            if (offre.getDateLimite() == null) {
                offre.setDateLimite(LocalDate.now().plusDays(dureePublicationJours));
            }
            if (!publicationAutomatique) {
                log.info("Offre {} créée en brouillon depuis la demande {} : en attente de validation RH",
                        offre.getReference(), idDemande);
                return;
            }
            List<String> manquants = offreService.champsManquants(offre);
            if (!manquants.isEmpty()) {
                log.warn("Offre {} laissée en brouillon (à compléter par la RH) : {}", offre.getReference(), manquants);
                return;
            }
            offreService.valider(offre.getIdOffre(), idValidateur);
            offreService.publier(offre.getIdOffre());
            log.info("Offre {} créée et publiée automatiquement depuis la demande {}", offre.getReference(), idDemande);
        } catch (RuntimeException e) {
            log.warn("Création automatique de l'offre impossible pour la demande {} : {} (création manuelle possible)",
                    idDemande, e.getMessage());
        }
    }
}
