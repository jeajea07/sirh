package com.gestionrh.sirh.scheduler;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.gestionrh.sirh.service.AbsenceService;
import com.gestionrh.sirh.service.AnomalieService;
import com.gestionrh.sirh.service.CalculTempsService;
import com.gestionrh.sirh.service.CongeService;
import com.gestionrh.sirh.service.SoldeService;
import com.gestionrh.sirh.service.TempsParametres;

/**
 * Blocs 6 et 7 — traitement de nuit : calcul des journées (RG-TT), escalades (RG-AN04, RG-DC12),
 * avancement des congés (VALIDEE → EN_COURS → PRISE), relance des justificatifs (RG-AB03),
 * allocation annuelle des permissions (RG-SC11).
 */
@Component
public class TempsCongesScheduler {

    private static final Logger log = LoggerFactory.getLogger(TempsCongesScheduler.class);

    private final CalculTempsService calcul;
    private final AnomalieService anomalies;
    private final CongeService conges;
    private final AbsenceService absences;
    private final SoldeService soldes;
    private final TempsParametres parametres;

    public TempsCongesScheduler(CalculTempsService calcul, AnomalieService anomalies, CongeService conges,
                                AbsenceService absences, SoldeService soldes, TempsParametres parametres) {
        this.calcul = calcul;
        this.anomalies = anomalies;
        this.conges = conges;
        this.absences = absences;
        this.soldes = soldes;
        this.parametres = parametres;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void auDemarrage() {
        executer();
    }

    @Scheduled(cron = "${app.temps.calcul-cron:0 30 1 * * *}")
    public void executer() {
        LocalDate aujourdhui = LocalDate.now();
        etape("allocation des permissions", () -> soldes.attributionAnnuelle(aujourdhui.getYear()));
        etape("avancement des congés", () -> conges.avancerStatuts(aujourdhui));
        etape("calcul des journées", () -> {
            int n = 0;
            for (int i = parametres.joursRecalcul; i >= 1; i--) n += calcul.calculerJour(aujourdhui.minusDays(i));
            return n;
        });
        etape("escalade des anomalies", anomalies::escalader);
        etape("escalade des demandes de congé", conges::escalader);
        etape("relance des justificatifs", () -> absences.relancerJustificatifs(aujourdhui));
    }

    private void etape(String nom, java.util.function.IntSupplier traitement) {
        try {
            int n = traitement.getAsInt();
            if (n > 0) log.info("Temps et congés — {} : {} élément(s).", nom, n);
        } catch (RuntimeException e) {
            log.warn("Temps et congés — {} impossible : {}", nom, e.getMessage());
        }
    }
}
