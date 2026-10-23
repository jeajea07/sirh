package com.gestionrh.sirh.controller.recrutement;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.recrutement.NotificationRecrutementRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Compteur limité aux écrans du recrutement sans modifier le gabarit partagé du bloc 1. */
@ControllerAdvice(basePackages = "com.gestionrh.sirh.controller.recrutement")
public class NotificationRecrutementAdvice {

    private final EmployeRepository employes;
    private final NotificationRecrutementRepository notifications;

    public NotificationRecrutementAdvice(EmployeRepository employes, NotificationRecrutementRepository notifications) {
        this.employes = employes;
        this.notifications = notifications;
    }

    @ModelAttribute
    public void ajouterCompteur(Authentication authentication, Model model) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) return;
        EmployeModel employe = employes.trouverParLogin(authentication.getName()).orElse(null);
        if (employe == null) return;
        model.addAttribute("nbNotificationsRecrutement",
                notifications.countByIdDestinataireEmployeAndLuFalse(employe.getId()));
    }
}
