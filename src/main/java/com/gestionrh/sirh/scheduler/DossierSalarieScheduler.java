package com.gestionrh.sirh.scheduler;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gestionrh.sirh.service.DossierDocumentService;
import com.gestionrh.sirh.service.StatutSalarieService;

/**
 * Bloc 5 — traitement quotidien du dossier salarié :
 * sorties arrivées à échéance (RG-SO02) et validité des pièces (RG-DO05).
 */
@Component
public class DossierSalarieScheduler {

    private static final Logger log = LoggerFactory.getLogger(DossierSalarieScheduler.class);

    private final StatutSalarieService statuts;
    private final DossierDocumentService pieces;

    public DossierSalarieScheduler(StatutSalarieService statuts, DossierDocumentService pieces) {
        this.statuts = statuts;
        this.pieces = pieces;
    }

    /** Rattrapage au démarrage, si l'application était arrêtée lors du passage quotidien. */
    @EventListener(ApplicationReadyEvent.class)
    public void auDemarrage() {
        executer();
    }

    @Scheduled(cron = "${app.dossier.cron:0 15 0 * * *}")
    public void executer() {
        LocalDate jour = LocalDate.now();
        try {
            int sorties = statuts.appliquerSortiesEchues(jour);
            if (sorties > 0) {
                log.info("{} sortie(s) de salarié appliquée(s).", sorties);
            }
        } catch (RuntimeException e) {
            log.warn("Application des sorties impossible : {}", e.getMessage());
        }
        try {
            int changes = pieces.mettreAJourValidite(jour);
            if (changes > 0) {
                log.info("{} pièce(s) du dossier salarié passée(s) à renouveler ou expirée(s).", changes);
            }
        } catch (RuntimeException e) {
            log.warn("Contrôle de validité des pièces impossible : {}", e.getMessage());
        }
    }
}
