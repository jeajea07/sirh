package com.gestionrh.sirh.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.NotificationModel;
import com.gestionrh.sirh.repository.NotificationRepository;
import com.gestionrh.sirh.service.dto.NotificationVue;

@Service
public class NotificationService {

    private static final String PREFIXE_A_TRAITER = "Nouvelle demande à traiter";
    private static final DateTimeFormatter JOUR_HEURE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final NotificationRepository notifications;

    public NotificationService(NotificationRepository notifications) {
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<NotificationVue> recentes(Integer idEmploye, boolean estRh) {
        List<NotificationVue> vues = new ArrayList<>();
        for (NotificationModel n : notifications.findTop10ByIdDestinataireOrderByIdDesc(idEmploye)) {
            vues.add(new NotificationVue(n.getMessages(), n.getDateEnvoi().format(JOUR_HEURE),
                    lien(n, estRh), n.isLue()));
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

    private static String lien(NotificationModel n, boolean estRh) {
        if (n.getIdDemandeBesoin() == null) {
            return null;
        }
        boolean aTraiter = n.getMessages() != null && n.getMessages().startsWith(PREFIXE_A_TRAITER);
        if (aTraiter) {
            return estRh ? "/rh/demandes/" + n.getIdDemandeBesoin() : null;
        }
        return "/demandes/" + n.getIdDemandeBesoin();
    }
}
