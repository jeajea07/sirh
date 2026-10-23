package com.gestionrh.sirh.controller.recrutement;

import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.recrutement.NotificationRecrutement;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.recrutement.NotificationRecrutementRepository;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/recrutement/notifications")
@PreAuthorize("hasRole('RH')")
public class NotificationRecrutementController {

    private final NotificationRecrutementRepository notifications;
    private final EmployeRepository employes;

    public NotificationRecrutementController(NotificationRecrutementRepository notifications, EmployeRepository employes) {
        this.notifications = notifications;
        this.employes = employes;
    }

    @GetMapping
    public String liste(Authentication authentication, Model model) {
        EmployeModel rh = trouverRh(authentication);
        List<NotificationRecrutement> liste = notifications
                .findByIdDestinataireEmployeOrderByLuAscDateCreationDesc(rh.getId());
        model.addAttribute("notificationsRecrutement", liste);
        model.addAttribute("nbNotificationsRecrutement", liste.stream().filter(n -> !Boolean.TRUE.equals(n.getLu())).count());
        model.addAttribute("titrePage", "Mes notifications de recrutement");
        return "recrutement/notification/liste";
    }

    @PostMapping("/{id}/lue")
    public String marquerLue(@PathVariable Integer id, Authentication authentication, RedirectAttributes ra) {
        EmployeModel rh = trouverRh(authentication);
        notifications.findById(id).filter(n -> rh.getId().equals(n.getIdDestinataireEmploye())).ifPresent(n -> {
            n.setLu(Boolean.TRUE);
            notifications.save(n);
        });
        ra.addFlashAttribute("message", "Notification marquée comme lue.");
        return "redirect:/recrutement/notifications";
    }

    private EmployeModel trouverRh(Authentication authentication) {
        return employes.trouverParLogin(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Le compte RH connecté n'est pas associé à un employé."));
    }
}
