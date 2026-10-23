package com.gestionrh.sirh.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.dto.NotificationVue;
import com.gestionrh.sirh.model.NotificationModel;
import com.gestionrh.sirh.repository.NotificationRepository;

@Service
public class NotificationService {

    private static final String PREFIXE_A_TRAITER = "Nouvelle demande à traiter";
    private static final String PREFIXE_DEMANDEUR = "Votre demande";
    private static final DateTimeFormatter JOUR_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<NotificationVue> recentes(Integer idEmploye, String role) {
        List<NotificationVue> vues = new ArrayList<>();
        for (NotificationModel n : notifications.findTop10ByIdDestinataireOrderByIdDesc(idEmploye)) {
            vues.add(new NotificationVue(n.getMessages(), n.getDateEnvoi().format(JOUR_HEURE),
                    lien(n, role), n.isLue()));
        }
        return vues;
    }

    @Transactional(readOnly = true)
    public long nonLues(Integer idEmploye) {
        return notifications.countByIdDestinataireAndLueFalse(idEmploye);
    }

    @Transactional
    public void toutMarquerLues(Integer idEmploye) {
        notifications.marquerToutesLues(idEmploye);
    }

    @Transactional
    public void notifier(Integer idDestinataire, String typeObjet, Integer idObjet, String message) {
        if (idDestinataire == null || message == null || message.isBlank()) {
            return;
        }
        NotificationModel n = new NotificationModel();
        n.setIdDestinataire(idDestinataire);
        n.setTypeObjet(typeObjet);
        n.setIdObjet(idObjet);
        n.setMessages(message);
        n.setDateEnvoi(java.time.LocalDateTime.now());
        n.setLue(false);
        notifications.save(n);
    }

    private static String lien(NotificationModel n, String role) {
        // Le type d'objet du Bloc 4 doit être testé avant l'ancien champ demande.
        if (n.getTypeObjet() != null && n.getIdObjet() != null) {
            return switch (n.getTypeObjet()) {
                case "CONTRAT" -> "RH".equals(role) ? "/rh/contrats/" + n.getIdObjet() : "/mes-taches";
                case "ONBOARDING" -> "RH".equals(role) ? "/rh/onboardings/" + n.getIdObjet() : "/mes-taches";
                case "TACHE" -> "/mes-taches";
                case "DOCUMENT" -> "RH".equals(role) ? "/rh/documents" : "/mes-taches";
                case "DECLARATION" -> "RH".equals(role) ? "/rh/formalites/" + n.getIdObjet() : "/mes-taches";
                default -> null;
            };
        }
        if (n.getIdDemandeBesoin() == null || n.getMessages() == null) {
            return null;
        }
        if (n.getMessages().startsWith(PREFIXE_A_TRAITER)) {
            if ("RH".equals(role)) {
                return "/rh/demandes/" + n.getIdDemandeBesoin();
            }
            if ("FINANCE".equals(role)) {
                return "/finance/demandes/" + n.getIdDemandeBesoin();
            }
            if ("DG".equals(role)) {
                return "/dg/demandes/" + n.getIdDemandeBesoin();
            }
            return null;
        }
        if (n.getMessages().startsWith(PREFIXE_DEMANDEUR)) {
            return "/demandes/" + n.getIdDemandeBesoin();
        }
        return null;
    }
}
