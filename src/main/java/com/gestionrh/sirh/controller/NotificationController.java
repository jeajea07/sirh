package com.gestionrh.sirh.controller;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.service.NotificationService;

@Controller
public class NotificationController {

    private final NotificationService service;
    private final EmployeRepository employes;

    public NotificationController(NotificationService service, EmployeRepository employes) {
        this.service = service;
        this.employes = employes;
    }

    @PostMapping("/notifications/lues")
    public String toutMarquerLues(Authentication auth, HttpServletRequest requete) {
        EmployeModel moi = employes.trouverParLogin(auth.getName()).orElse(null);
        if (moi != null) {
            service.toutMarquerLues(moi.getId());
        }
        String retour = pageDOrigine(requete);
        return "redirect:" + (retour != null ? retour : "/accueil");
    }

    /**
     * Chemin (sans le contexte de l'application) de la page où se trouvait l'utilisateur, d'après l'en-tête Referer.
     * On n'utilise pas request.getRequestURI() dans la JSP : après un forward, il vaut /WEB-INF/jsp/....
     */
    private static String pageDOrigine(HttpServletRequest requete) {
        String referer = requete.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(referer);
            String hote = uri.getHost();
            if (hote == null || !hote.equalsIgnoreCase(requete.getServerName())) {
                return null;
            }
            String chemin = uri.getRawPath();
            String contexte = requete.getContextPath();
            if (chemin == null || !chemin.startsWith(contexte)) {
                return null;
            }
            chemin = chemin.substring(contexte.length());
            if (chemin.isEmpty() || !chemin.startsWith("/") || chemin.startsWith("//")) {
                return null;
            }
            return uri.getRawQuery() == null ? chemin : chemin + "?" + uri.getRawQuery();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
