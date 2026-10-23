package com.gestionrh.sirh.scheduler.recrutement;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.service.recrutement.DestinatairesRhResolver;
import com.gestionrh.sirh.service.recrutement.JoursOuvrablesService;
import com.gestionrh.sirh.service.recrutement.NotificationRecrutementService;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RappelCandidaturesEnRetardScheduler {

    private static final Logger log = LoggerFactory.getLogger(RappelCandidaturesEnRetardScheduler.class);
    private final CandidatureRepository candidatures;
    private final JoursOuvrablesService joursOuvrables;
    private final DestinatairesRhResolver destinatairesRh;
    private final NotificationRecrutementService notifications;

    public RappelCandidaturesEnRetardScheduler(CandidatureRepository candidatures,
            JoursOuvrablesService joursOuvrables, DestinatairesRhResolver destinatairesRh,
            NotificationRecrutementService notifications) {
        this.candidatures = candidatures;
        this.joursOuvrables = joursOuvrables;
        this.destinatairesRh = destinatairesRh;
        this.notifications = notifications;
    }

    @Scheduled(cron = "${app.recrutement.rappel-cron:0 0 8 * * *}", zone = "Indian/Antananarivo")
    public void envoyerRappelQuotidien() {
        LocalDate aujourdhui = LocalDate.now(java.time.ZoneId.of("Indian/Antananarivo"));
        List<Candidature> retards = candidatures.findByStatut(CandidatureStatut.A_ETUDIER).stream()
                .filter(c -> joursOuvrables.estEnRetard(c.getDateReception(), aujourdhui)).toList();
        if (retards.isEmpty()) return;
        List<Integer> rhs = destinatairesRh.trouverDestinataires();
        if (rhs.isEmpty()) {
            log.warn("Rappel de candidatures en retard ignoré : aucun destinataire RH n'est configuré.");
            return;
        }
        String liste = retards.stream().map(c -> ligne(c, aujourdhui)).collect(Collectors.joining("\n"));
        String type = "RAPPEL_RETARDS_" + aujourdhui.format(DateTimeFormatter.BASIC_ISO_DATE);
        for (Integer idRh : rhs) {
            try {
                notifications.notifierEmployeUneFoisParJour(idRh, type, "Rappel : dossiers de recrutement en retard",
                        "Les candidatures suivantes dépassent le délai de traitement de cinq jours ouvrables :\n" + liste, null);
            } catch (RuntimeException e) {
                log.warn("Impossible de créer le rappel quotidien pour un destinataire RH (id={})", idRh, e);
            }
        }
    }

    private String ligne(Candidature c, LocalDate aujourdHui) {
        String candidat = c.getCandidat().getPrenom() + " " + c.getCandidat().getNom();
        String offre = c.getOffre() == null ? "Réserve" : c.getOffre().getReference();
        return "%s — %s — %s — %d jour(s) de retard".formatted(c.getNumero(), candidat, offre,
                joursOuvrables.joursDeRetard(c.getDateReception(), aujourdHui));
    }
}
