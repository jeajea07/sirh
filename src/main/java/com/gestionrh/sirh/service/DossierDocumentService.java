package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.DocumentModel;
import com.gestionrh.sirh.model.TypeDocumentAdministratifModel;
import com.gestionrh.sirh.repository.DocumentRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.TypeDocumentAdministratifRepository;
import com.gestionrh.sirh.service.dto.PieceDossierVue;
import com.gestionrh.sirh.util.Formats;

/**
 * Bloc 5 §10 — validité des pièces du dossier salarié : complétude et surveillance des expirations.
 */
@Service
public class DossierDocumentService {

    private final DocumentRepository documents;
    private final TypeDocumentAdministratifRepository types;
    private final EmployeRepository employes;
    private final NotificationService notifications;

    public DossierDocumentService(DocumentRepository documents, TypeDocumentAdministratifRepository types,
                                  EmployeRepository employes, NotificationService notifications) {
        this.documents = documents;
        this.types = types;
        this.employes = employes;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<TypeDocumentAdministratifModel> typesActifs() {
        return types.findByActifTrueOrderByLibelleAsc();
    }

    /** RG-DO02 : état de chaque pièce obligatoire pour ce salarié. */
    @Transactional(readOnly = true)
    public List<PieceDossierVue> piecesObligatoires(Integer idEmploye) {
        List<PieceDossierVue> pieces = new ArrayList<>();
        for (TypeDocumentAdministratifModel type : types.findByActifTrueAndObligatoireTrueOrderByLibelleAsc()) {
            List<DocumentModel> versions = documents.versionsActives(idEmploye, type.getId());
            DocumentModel retenu = versions.stream()
                    .filter(d -> !"REJETE".equals(d.getStatutValidation()))
                    .findFirst().orElse(null);
            String etat;
            if (retenu == null) {
                etat = versions.isEmpty() ? "MANQUANT" : "REJETE";
                retenu = versions.isEmpty() ? null : versions.get(0);
            } else {
                etat = switch (retenu.getStatutValidation()) {
                    case "VERIFIE" -> "VALIDE";
                    case "A_RENOUVELER" -> "A_RENOUVELER";
                    case "EXPIRE" -> "EXPIRE";
                    case "RECU" -> "EN_VERIFICATION";
                    default -> "MANQUANT";
                };
            }
            pieces.add(new PieceDossierVue(type.getCode(), type.getLibelle(), etat,
                    retenu == null || retenu.getDateExpiration() == null ? "—" : Formats.date(retenu.getDateExpiration()),
                    retenu == null ? null : retenu.getId(), type.getId()));
        }
        return pieces;
    }

    /**
     * RG-DO05 : passage automatique à A_RENOUVELER puis EXPIRE selon la date d'expiration et le délai d'alerte.
     * @return le nombre de pièces dont le statut a changé
     */
    @Transactional
    public int mettreAJourValidite(LocalDate jour) {
        int changes = 0;
        for (DocumentModel d : documents.documentsASurveiller()) {
            String nouveau = statutSelonDate(d, jour);
            if (nouveau.equals(d.getStatutValidation())) {
                continue;
            }
            d.setStatutValidation(nouveau);
            documents.save(d);
            changes++;
            notifier(d, nouveau);
        }
        return changes;
    }

    /** Statut d'une pièce vérifiée à une date donnée : VERIFIE, A_RENOUVELER ou EXPIRE. */
    public static String statutSelonDate(DocumentModel d, LocalDate jour) {
        LocalDate expiration = d.getDateExpiration();
        if (expiration == null) {
            return "VERIFIE";
        }
        if (expiration.isBefore(jour)) {
            return "EXPIRE";
        }
        int delai = d.getTypeAdministratif() == null ? 30 : d.getTypeAdministratif().getDelaiAlerteJours();
        return expiration.minusDays(delai).isAfter(jour) ? "VERIFIE" : "A_RENOUVELER";
    }

    private void notifier(DocumentModel d, String statut) {
        if (d.getEmploye() == null) {
            return;
        }
        String piece = d.getTypeAdministratif() == null ? d.getTitre() : d.getTypeAdministratif().getLibelle();
        String message = "EXPIRE".equals(statut)
                ? piece + " de " + d.getEmploye().getNomComplet() + " a expiré le "
                        + Formats.date(d.getDateExpiration()) + " : dossier incomplet."
                : piece + " de " + d.getEmploye().getNomComplet() + " expire le "
                        + Formats.date(d.getDateExpiration()) + " : à renouveler.";
        notifications.notifier(d.getEmploye().getId(), "DOCUMENT", d.getId(),
                "EXPIRE".equals(statut)
                        ? "Votre pièce « " + piece + " » a expiré : déposez-en une nouvelle dans Mon dossier."
                        : "Votre pièce « " + piece + " » expire le " + Formats.date(d.getDateExpiration())
                                + " : pensez à la renouveler dans Mon dossier.");
        employes.validateursParRole("RH").forEach(rh ->
                notifications.notifier(rh.getId(), "DOCUMENT", d.getId(), message));
    }
}
