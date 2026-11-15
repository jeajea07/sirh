package com.gestionrh.sirh.scheduler;

import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.service.ContratService;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Bloc 4 — suivi de la date d'effet : un contrat SIGNE passe EN_VIGUEUR le jour de sa date d'effet,
 * sans attendre une action manuelle de la RH. Les règles restent celles de {@link ContratService#activer}.
 */
@Component
public class ActivationContratsScheduler {

    private static final Logger log = LoggerFactory.getLogger(ActivationContratsScheduler.class);

    private final ContratRepository contrats;
    private final ContratService service;

    public ActivationContratsScheduler(ContratRepository contrats, ContratService service) {
        this.contrats = contrats;
        this.service = service;
    }

    /** Rattrapage au démarrage (application arrêtée le jour de la date d'effet). */
    @EventListener(ApplicationReadyEvent.class)
    public void auDemarrage() {
        activerContratsArrivesAEcheance();
    }

    @Scheduled(cron = "${app.contrat.activation-cron:0 5 0 * * *}")
    public void activerContratsArrivesAEcheance() {
        for (ContratModel c : contrats.signesActivables(LocalDate.now())) {
            if (c.getCreePar() == null) {
                log.warn("Contrat {} signé mais sans rédacteur : activation manuelle requise.", c.getReference());
                continue;
            }
            try {
                service.activer(c.getId(), c.getCreePar());
                log.info("Contrat {} activé automatiquement à sa date d'effet.", c.getReference());
            } catch (RuntimeException e) {
                log.warn("Activation automatique impossible pour le contrat {} : {}", c.getReference(), e.getMessage());
            }
        }
    }
}
