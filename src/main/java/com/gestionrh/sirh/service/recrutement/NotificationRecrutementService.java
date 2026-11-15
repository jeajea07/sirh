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
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.util.HtmlUtils;
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
    private final String nomExpediteur;
    private final String urlSite;

    public NotificationRecrutementService(NotificationRecrutementRepository notifications,
            CandidatRepository candidats, JavaMailSender mailSender,
            @Value("${app.mail.actif:false}") boolean mailActif,
            @Value("${app.mail.expediteur:no-reply@example.invalid}") String expediteur,
            @Value("${app.mail.nom-expediteur:TeamUp Recrutement}") String nomExpediteur,
            @Value("${app.site.url:http://localhost:8080}") String urlSite) {
        this.notifications = notifications;
        this.candidats = candidats;
        this.mailSender = mailSender;
        this.mailActif = mailActif;
        this.expediteur = expediteur;
        this.nomExpediteur = nomExpediteur;
        this.urlSite = urlSite.endsWith("/") ? urlSite.substring(0, urlSite.length() - 1) : urlSite;
        log.info("Envoi des e-mails aux candidats : {}", mailActif ? "ACTIF (expéditeur " + expediteur + ")"
                : "simulé (app.mail.actif=false) — voir config/mail.properties.example");
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
        if (n.getTentatives() >= LIMITE_TENTATIVES) return;
        n.setTentatives(n.getTentatives() + 1);
        try {
            if (n.getCanal() == CanalNotification.EMAIL) {
                envoyerCourriel(n);
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

    private void envoyerCourriel(NotificationRecrutement n) {
        Candidat c = candidats.findById(n.getIdCandidat()).orElseThrow(() ->
                new RessourceIntrouvableException("Candidat introuvable pour l'envoi de la notification."));
        if (!mailActif) {
            String contenu = n.getTypeNotification().startsWith("RAPPEL_RETARDS_")
                    ? "[contenu nominatif masqué dans les journaux]" : n.getMessage();
            log.info("Simulation d'envoi e-mail — objet: {} ; contenu: {}", n.getObjet(), contenu);
            return;
        }
        try {
            MimeMessage mail = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mail, true, "UTF-8");
            helper.setFrom(expediteur, nomExpediteur);
            helper.setTo(c.getEmail());
            helper.setSubject(n.getObjet() == null ? "Votre candidature" : n.getObjet());
            String message = sansSalutation(n.getMessage());
            helper.setText(corpsTexte(c, message), corpsHtml(c, n.getObjet(), message));
            mailSender.send(mail);
            log.info("E-mail envoyé au candidat id={} — {}", c.getIdCandidat(), n.getTypeNotification());
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new MailPreparationException("Préparation de l'e-mail impossible : " + e.getMessage(), e);
        }
    }

    /** Le gabarit salue déjà le candidat par son prénom : on retire un « Bonjour, » de tête. */
    private static String sansSalutation(String message) {
        if (message == null) return "";
        String m = message.strip();
        if (m.regionMatches(true, 0, "Bonjour,", 0, 8)) {
            m = m.substring(8).strip();
            if (!m.isEmpty()) m = Character.toUpperCase(m.charAt(0)) + m.substring(1);
        }
        return m;
    }

    private String corpsTexte(Candidat c, String message) {
        return "Bonjour " + (c.getPrenom() == null ? "" : c.getPrenom()) + ",\n\n" + message
                + "\n\nSuivre votre candidature : " + urlSite + "/connexion"
                + "\nOffres d'emploi : " + urlSite + "/offres"
                + "\n\n— " + nomExpediteur + "\nCet e-mail est envoyé automatiquement, merci de ne pas y répondre.";
    }

    /** Gabarit HTML simple (styles en ligne pour la compatibilité des messageries). */
    private String corpsHtml(Candidat c, String objet, String message) {
        String msg = HtmlUtils.htmlEscape(message == null ? "" : message).replace("\n", "<br>");
        String prenom = HtmlUtils.htmlEscape(c.getPrenom() == null ? "" : c.getPrenom());
        String titre = HtmlUtils.htmlEscape(objet == null ? "Votre candidature" : objet);
        return "<!doctype html><html lang=\"fr\"><body style=\"margin:0;background:#F5F3EE;font-family:Segoe UI,Arial,sans-serif;color:#0B1224\">"
                + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#F5F3EE;padding:24px 0\"><tr><td align=\"center\">"
                + "<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;width:100%;background:#ffffff;border:1px solid #D9D4C7;border-radius:6px;overflow:hidden\">"
                + "<tr><td style=\"background:#081227;border-bottom:3px solid #F5A300;padding:18px 24px;font-size:20px;font-weight:800;color:#ffffff\">TeamUp <span style=\"color:#FFC83D\">Carrières</span></td></tr>"
                + "<tr><td style=\"padding:28px 24px 8px\"><h1 style=\"margin:0 0 14px;font-size:20px\">" + titre + "</h1>"
                + "<p style=\"margin:0 0 12px;font-size:15px;line-height:1.6\">Bonjour " + prenom + ",</p>"
                + "<p style=\"margin:0 0 20px;font-size:15px;line-height:1.6\">" + msg + "</p>"
                + "<p style=\"margin:0 0 24px\"><a href=\"" + urlSite + "/connexion\" style=\"display:inline-block;background:#F5A300;color:#081227;font-weight:700;text-decoration:none;padding:11px 18px;border-radius:6px\">Accéder à mon espace candidat</a></p>"
                + "</td></tr><tr><td style=\"padding:16px 24px;background:#ECE9E1;font-size:12px;color:#636A7E\">"
                + "Cet e-mail est envoyé automatiquement par " + HtmlUtils.htmlEscape(nomExpediteur) + ", merci de ne pas y répondre. "
                + "<a href=\"" + urlSite + "/offres\" style=\"color:#0B1224\">Voir nos offres d'emploi</a></td></tr>"
                + "</table></td></tr></table></body></html>";
    }

    private String limite(String texte, int longueur) {
        if (texte == null) return "Erreur d'envoi non précisée.";
        return texte.length() <= longueur ? texte : texte.substring(0, longueur);
    }
}
