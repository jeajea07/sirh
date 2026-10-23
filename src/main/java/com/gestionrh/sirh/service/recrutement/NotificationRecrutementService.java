package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.enums.recrutement.CanalNotification;
import com.gestionrh.sirh.enums.recrutement.StatutEnvoi;
import com.gestionrh.sirh.exception.recrutement.RessourceIntrouvableException;
import com.gestionrh.sirh.model.recrutement.Candidat;
import com.gestionrh.sirh.model.recrutement.NotificationRecrutement;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;
import com.gestionrh.sirh.repository.recrutement.NotificationRecrutementRepository;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Enregistre chaque notification avant envoi ; les échecs d'e-mail restent indépendants du métier. */
@Service
public class NotificationRecrutementService {

    private static final Logger log = LoggerFactory.getLogger(NotificationRecrutementService.class);
    private static final int LIMITE_TENTATIVES = 5;

    private final NotificationRecrutementRepository notifications;
    private final CandidatRepository candidats;
    private final JavaMailSender mailSender;
    private final boolean mailActif;
    private final String expediteur;

    public NotificationRecrutementService(NotificationRecrutementRepository notifications,
            CandidatRepository candidats, JavaMailSender mailSender,
            @Value("${app.mail.actif:false}") boolean mailActif,
            @Value("${app.mail.expediteur:no-reply@example.invalid}") String expediteur) {
        this.notifications = notifications;
        this.candidats = candidats;
        this.mailSender = mailSender;
        this.mailActif = mailActif;
        this.expediteur = expediteur;
    }

    @Transactional
    public NotificationRecrutement notifierEmploye(Integer idEmploye, String type, String objet,
            String message, Integer idOffre, Integer idCandidature) {
        if (idEmploye == null) throw new IllegalArgumentException("Le destinataire RH est obligatoire.");
        if (dejaNotifie(idCandidature, type)) return null;
        NotificationRecrutement n = nouvelle(type, objet, message, idOffre, idCandidature);
        n.setCanal(CanalNotification.INTERNE);
        n.setIdDestinataireEmploye(idEmploye);
        notifications.saveAndFlush(n);
        traiter(n);
        return n;
    }

    @Transactional
    public NotificationRecrutement notifierCandidat(Integer idCandidat, String type, String objet,
            String message, Integer idOffre, Integer idCandidature) {
        if (idCandidat == null) throw new IllegalArgumentException("Le destinataire candidat est obligatoire.");
        if (dejaNotifie(idCandidature, type)) return null;
        NotificationRecrutement n = nouvelle(type, objet, message, idOffre, idCandidature);
        n.setCanal(CanalNotification.EMAIL);
        n.setIdCandidat(idCandidat);
        notifications.saveAndFlush(n);
        traiter(n);
        return n;
    }

    /** Envoie des identifiants temporaires sans les conserver dans le corps de notification en base. */
    @Transactional
    public NotificationRecrutement notifierAccesTestCandidat(Integer idCandidat, String objet,
            String messageSecret, Integer idOffre, Integer idCandidature) {
        String type = "ACCES_TEST_CANDIDAT";
        if (idCandidat == null) throw new IllegalArgumentException("Le destinataire candidat est obligatoire.");
        if (dejaNotifie(idCandidature, type)) return null;
        NotificationRecrutement n = nouvelle(type, objet,
                "Les identifiants temporaires du test ont été envoyés par e-mail.", idOffre, idCandidature);
        n.setCanal(CanalNotification.EMAIL);
        n.setIdCandidat(idCandidat);
        n.setStatutEnvoi(StatutEnvoi.EN_ATTENTE);
        notifications.saveAndFlush(n);
        traiter(n, messageSecret);
        if (n.getStatutEnvoi() == StatutEnvoi.ECHEC) {
            // Le mot de passe temporaire n'est volontairement pas stocké pour permettre une relance.
            n.setTentatives(LIMITE_TENTATIVES);
            notifications.save(n);
        }
        return n;
    }

    @Transactional
    public NotificationRecrutement notifierEmployeUneFoisParJour(Integer idEmploye, String type, String objet,
            String message, Integer idOffre) {
        LocalDate aujourdhui = LocalDate.now();
        LocalDateTime debut = aujourdhui.atStartOfDay();
        LocalDateTime fin = debut.plusDays(1);
        if (notifications.existsByIdDestinataireEmployeAndTypeNotificationAndDateCreationBetween(
                idEmploye, type, debut, fin)) return null;
        return notifierEmploye(idEmploye, type, objet, message, idOffre, null);
    }

    @Transactional
    @Scheduled(fixedDelayString = "${app.recrutement.notification-delai-relance-ms:120000}")
    public void relancerEnvoisEnAttente() {
        List<NotificationRecrutement> aRelancer = notifications
                .findByStatutEnvoiInAndTentativesLessThanOrderByDateCreationAsc(
                        List.of(StatutEnvoi.EN_ATTENTE, StatutEnvoi.ECHEC), LIMITE_TENTATIVES);
        for (NotificationRecrutement n : aRelancer) {
            try {
                traiter(n);
            } catch (RuntimeException e) {
                log.error("Erreur inattendue pendant la relance d'une notification id={}", n.getIdNotificationRecrutement(), e);
            }
        }
    }

    private NotificationRecrutement nouvelle(String type, String objet, String message,
            Integer idOffre, Integer idCandidature) {
        if (type == null || type.isBlank() || type.length() > 40) {
            throw new IllegalArgumentException("Le type de notification est obligatoire (40 caractères maximum).");
        }
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Le message est obligatoire.");
        NotificationRecrutement n = new NotificationRecrutement();
        n.setTypeNotification(type);
        n.setObjet(objet);
        n.setMessage(message);
        n.setIdOffre(idOffre);
        n.setIdCandidature(idCandidature);
        n.setStatutEnvoi(StatutEnvoi.EN_ATTENTE);
        n.setTentatives(0);
        n.setLu(Boolean.FALSE);
        return n;
    }

    private boolean dejaNotifie(Integer idCandidature, String type) {
        return idCandidature != null && notifications.existsByIdCandidatureAndTypeNotification(idCandidature, type);
    }

    private void traiter(NotificationRecrutement n) {
        traiter(n, n.getMessage());
    }

    private void traiter(NotificationRecrutement n, String messageCourriel) {
        if (n.getTentatives() >= LIMITE_TENTATIVES) return;
        n.setTentatives(n.getTentatives() + 1);
        try {
            if (n.getCanal() == CanalNotification.EMAIL) {
                envoyerCourriel(n, messageCourriel);
            }
            n.setStatutEnvoi(StatutEnvoi.ENVOYE);
            n.setErreur(null);
            n.setDateEnvoi(LocalDateTime.now());
        } catch (RuntimeException e) {
            n.setStatutEnvoi(StatutEnvoi.ECHEC);
            n.setErreur(limite(e.getMessage(), 2000));
            log.warn("Échec d'envoi de notification id={} (tentative {}) : {}",
                    n.getIdNotificationRecrutement(), n.getTentatives(), e.getClass().getSimpleName());
        }
        notifications.save(n);
    }

    private void envoyerCourriel(NotificationRecrutement n, String messageCourriel) {
        Candidat c = candidats.findById(n.getIdCandidat()).orElseThrow(() ->
                new RessourceIntrouvableException("Candidat introuvable pour l'envoi de la notification."));
        if (!mailActif) {
            String contenu = n.getTypeNotification().startsWith("RAPPEL_RETARDS_")
                    || "ACCES_TEST_CANDIDAT".equals(n.getTypeNotification())
                    ? "[contenu sensible masqué dans les journaux]" : messageCourriel;
            log.info("Simulation d'envoi e-mail — objet: {} ; contenu: {}", n.getObjet(), contenu);
            return;
        }
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(expediteur);
        mail.setTo(c.getEmail());
        mail.setSubject(n.getObjet());
        mail.setText(messageCourriel);
        mailSender.send(mail);
    }

    private String limite(String texte, int longueur) {
        if (texte == null) return "Erreur d'envoi non précisée.";
        return texte.length() <= longueur ? texte : texte.substring(0, longueur);
    }
}
