package com.gestionrh.sirh.controller.recrutement;

import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.exception.recrutement.RessourceIntrouvableException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Gestion des erreurs limitée aux contrôleurs du recrutement. */
@ControllerAdvice(basePackages = "com.gestionrh.sirh.controller.recrutement")
public class ErreursRecrutementAdvice {

    private static final Logger log = LoggerFactory.getLogger(ErreursRecrutementAdvice.class);

    @ExceptionHandler(RegleMetierException.class)
    public String regleMetier(RegleMetierException e, Model model, HttpServletRequest request, HttpServletResponse response) {
        return afficher(model, request, response, HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({RessourceIntrouvableException.class, NoResourceFoundException.class})
    public String introuvable(Exception e, Model model, HttpServletRequest request, HttpServletResponse response) {
        return afficher(model, request, response, HttpStatus.NOT_FOUND, "La ressource demandée est introuvable.");
    }

    @ExceptionHandler({BindException.class, MethodArgumentNotValidException.class})
    public String validation(Exception e, Model model, HttpServletRequest request, HttpServletResponse response) {
        String details = e instanceof BindException b
                ? b.getFieldErrors().stream().map(FieldError::getDefaultMessage).filter(s -> s != null && !s.isBlank())
                        .distinct().collect(Collectors.joining(" "))
                : "Vérifiez les champs obligatoires du formulaire.";
        return afficher(model, request, response, HttpStatus.BAD_REQUEST,
                details.isBlank() ? "Vérifiez les champs obligatoires du formulaire." : details);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tailleDepassee(MaxUploadSizeExceededException e, Model model, HttpServletRequest request, HttpServletResponse response) {
        return afficher(model, request, response, HttpStatus.PAYLOAD_TOO_LARGE, "Le fichier dépasse la taille maximale autorisée de 5 Mo.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public String contrainteDonnees(DataIntegrityViolationException e, Model model, HttpServletRequest request, HttpServletResponse response) {
        return afficher(model, request, response, HttpStatus.CONFLICT, "Cette opération entre en conflit avec des données déjà enregistrées.");
    }

    @ExceptionHandler(Exception.class)
    public String inattendue(Exception e, Model model, HttpServletRequest request, HttpServletResponse response) {
        log.error("Erreur inattendue dans le recrutement sur {}", request.getRequestURI(), e);
        return afficher(model, request, response, HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur inattendue s'est produite. Réessayez plus tard.");
    }

    private String afficher(Model model, HttpServletRequest request, HttpServletResponse response,
            HttpStatus statut, String message) {
        response.setStatus(statut.value());
        model.addAttribute("statutErreur", statut.value());
        model.addAttribute("messageErreur", message);
        model.addAttribute("urlRetour", request.getContextPath() + "/offres");
        model.addAttribute("titrePage", "Erreur");
        return "recrutement/erreur";
    }
}
