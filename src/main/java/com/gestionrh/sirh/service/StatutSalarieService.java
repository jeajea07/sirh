package com.gestionrh.sirh.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.AffectationModel;
import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.repository.AffectationRepository;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.UtilisateurRepository;
import com.gestionrh.sirh.util.Formats;

/**
 * Bloc 5 §6 et §12 — statut administratif du salarié : suspension, reprise, sortie et archivage.
 */
@Service
public class StatutSalarieService {

    private static final Logger log = LoggerFactory.getLogger(StatutSalarieService.class);

    public static final Map<String, String> MOTIFS_SORTIE = Map.of(
            "DEMISSION", "Démission",
            "LICENCIEMENT", "Licenciement",
            "FIN_CDD", "Fin de CDD",
            "RUPTURE_ESSAI", "Rupture de la période d'essai",
            "RETRAITE", "Départ à la retraite",
            "DECES", "Décès",
            "AUTRE", "Autre motif");

    private final EmployeRepository employes;
    private final ContratRepository contrats;
    private final AffectationRepository affectations;
    private final UtilisateurRepository utilisateurs;
    private final ContratService contratService;
    private final CarriereService carriere;
    private final NotificationService notifications;
    private final AuditService audit;

    public StatutSalarieService(EmployeRepository employes, ContratRepository contrats,
                                AffectationRepository affectations, UtilisateurRepository utilisateurs,
                                ContratService contratService, CarriereService carriere,
                                NotificationService notifications, AuditService audit) {
        this.employes = employes;
        this.contrats = contrats;
        this.affectations = affectations;
        this.utilisateurs = utilisateurs;
        this.contratService = contratService;
        this.carriere = carriere;
        this.notifications = notifications;
        this.audit = audit;
    }

    // ------------------------------------------------------------ suspension / reprise

    @Transactional
    public void suspendre(Integer idEmploye, String motif, LocalDate dateEffet, EmployeModel auteur) {
        EmployeModel e = charger(idEmploye);
        if (!"ACTIF".equals(e.getStatutAdministratif())) {
            throw new Bloc4Exception("Seul un salarié actif peut être suspendu.");
        }
        exigerTexte(motif, "Le motif de la suspension est obligatoire.");
        changerStatut(e, "SUSPENDU");
        contratEnVigueur(idEmploye).ifPresent(c -> {
            c.setStatutContractuel("SUSPENDU");
            contrats.save(c);
        });
        carriere.tracer(e, "SUSPENSION", dateEffet, "Suspension du contrat : " + motif,
                "ACTIF", "SUSPENDU", "SALARIE", idEmploye, auteur);
        audit.tracer("SALARIE", idEmploye, "SUSPENSION", "ACTIF", "SUSPENDU", motif, auteur);
    }

    @Transactional
    public void reprendre(Integer idEmploye, LocalDate dateEffet, EmployeModel auteur) {
        EmployeModel e = charger(idEmploye);
        if (!"SUSPENDU".equals(e.getStatutAdministratif())) {
            throw new Bloc4Exception("Seul un salarié suspendu peut reprendre.");
        }
        changerStatut(e, "ACTIF");
        contratEnVigueur(idEmploye).ifPresent(c -> {
            c.setStatutContractuel("ACTIF");
            contrats.save(c);
        });
        carriere.tracer(e, "REPRISE", dateEffet, "Reprise du travail", "SUSPENDU", "ACTIF",
                "SALARIE", idEmploye, auteur);
        audit.tracer("SALARIE", idEmploye, "REPRISE", "SUSPENDU", "ACTIF", null, auteur);
    }

    // ------------------------------------------------------------ sortie

    /** RG-SO01 : motif, date de notification et date de sortie. Appliquée tout de suite si la date est atteinte. */
    @Transactional
    public void enregistrerSortie(Integer idEmploye, String motif, String commentaire,
                                  LocalDate dateNotification, LocalDate dateSortie, EmployeModel auteur) {
        EmployeModel e = charger(idEmploye);
        String statut = e.getStatutAdministratif();
        if ("SORTI".equals(statut) || "EN_PREAVIS".equals(statut)) {
            throw new Bloc4Exception("Une sortie est déjà enregistrée pour ce salarié.");
        }
        if (motif == null || !MOTIFS_SORTIE.containsKey(motif)) {
            throw new Bloc4Exception("Le motif de sortie est invalide.");
        }
        if (dateNotification == null || dateSortie == null) {
            throw new Bloc4Exception("La date de notification et la date de sortie sont obligatoires.");
        }
        if (dateNotification.isAfter(LocalDate.now())) {
            throw new Bloc4Exception("La date de notification ne peut pas être dans le futur.");
        }
        if (dateSortie.isBefore(dateNotification)) {
            throw new Bloc4Exception("La date de sortie ne peut pas précéder la date de notification.");
        }
        if ("AUTRE".equals(motif)) {
            exigerTexte(commentaire, "Précisez le motif de sortie dans le commentaire.");
        }
        String libelle = MOTIFS_SORTIE.get(motif)
                + (commentaire == null || commentaire.isBlank() ? "" : " — " + commentaire.trim());
        e.setMotifSortie(libelle);
        e.setDateNotificationSortie(dateNotification);
        e.setDateSortie(dateSortie);
        e.setSortieEnregistreeParId(auteur.getId());
        changerStatut(e, "EN_PREAVIS");
        carriere.tracer(e, "SORTIE_ANNONCEE", dateNotification,
                "Sortie annoncée au " + Formats.date(dateSortie) + " : " + libelle,
                statut, "EN_PREAVIS", "SALARIE", idEmploye, auteur);
        audit.tracer("SALARIE", idEmploye, "SORTIE_ANNONCEE", statut, "EN_PREAVIS", libelle, auteur);
        affectationActive(idEmploye).map(AffectationModel::getResponsable)
                .filter(r -> r != null && !r.getId().equals(idEmploye))
                .ifPresent(r -> notifications.notifier(r.getId(), "SALARIE", idEmploye,
                        e.getNomComplet() + " quitte l'entreprise le " + Formats.date(dateSortie) + "."));
        if (!dateSortie.isAfter(LocalDate.now())) {
            appliquerSortie(e, auteur);
        }
    }

    /** RG-SO06 : une sortie s'annule tant que la date de sortie n'est pas atteinte. */
    @Transactional
    public void annulerSortie(Integer idEmploye, String motif, EmployeModel auteur) {
        EmployeModel e = charger(idEmploye);
        if (!"EN_PREAVIS".equals(e.getStatutAdministratif())) {
            throw new Bloc4Exception("Aucune sortie en attente à annuler.");
        }
        exigerTexte(motif, "Le motif d'annulation est obligatoire.");
        String ancienne = e.getMotifSortie();
        e.setMotifSortie(null);
        e.setDateSortie(null);
        e.setDateNotificationSortie(null);
        e.setSortieEnregistreeParId(null);
        changerStatut(e, "ACTIF");
        carriere.tracer(e, "SORTIE_ANNULEE", LocalDate.now(), "Sortie annulée : " + motif,
                ancienne, null, "SALARIE", idEmploye, auteur);
        audit.tracer("SALARIE", idEmploye, "SORTIE_ANNULEE", "EN_PREAVIS", "ACTIF", motif, auteur);
    }

    /** Traitement quotidien : applique les sorties dont la date est atteinte. */
    @Transactional
    public int appliquerSortiesEchues(LocalDate jour) {
        int n = 0;
        for (EmployeModel e : employes.findByStatutAdministratifAndDateSortieLessThanEqual("EN_PREAVIS", jour)) {
            EmployeModel auteur = e.getSortieEnregistreeParId() == null ? null
                    : employes.findById(e.getSortieEnregistreeParId()).orElse(null);
            if (auteur == null) {
                log.warn("Sortie de {} sans auteur enregistré : application manuelle requise.", e.getMatricule());
                continue;
            }
            appliquerSortie(e, auteur);
            n++;
        }
        return n;
    }

    /** RG-SO02 à RG-SO05 : contrat terminé, affectation clôturée, statut SORTI, accès coupés. */
    private void appliquerSortie(EmployeModel e, EmployeModel auteur) {
        LocalDate dateSortie = e.getDateSortie();
        ContratModel contrat = contratEnVigueur(e.getId()).orElse(null);
        if (contrat != null) {
            LocalDate fin = contrat.getDateDebut() != null && !dateSortie.isAfter(contrat.getDateDebut())
                    ? contrat.getDateDebut().plusDays(1) : dateSortie;
            // terminer() clôt aussi l'affectation active à la même date.
            contratService.terminer(contrat.getId(), "Sortie du salarié : " + e.getMotifSortie(), fin, auteur);
        } else {
            affectationActive(e.getId()).ifPresent(a -> {
                a.setStatut("TERMINEE");
                // Affectation jamais commencée : clôturée sans date de fin plutôt qu'avec une date inventée.
                a.setDateFin(a.getDateDebut() != null && !dateSortie.isAfter(a.getDateDebut()) ? null : dateSortie);
                a.setDateMaj(java.time.LocalDateTime.now());
                affectations.save(a);
            });
        }
        // Les contrats pas encore en vigueur (brouillon, à valider, à signer, signé) n'ont plus d'objet.
        for (ContratModel enCours : contrats.findByIdEmployeAndStatutWorkflowInOrderByDateDebutDesc(
                e.getId(), List.of("BROUILLON", "A_VALIDER", "A_SIGNER", "SIGNE"))) {
            contratService.annuler(enCours.getId(), "Sortie du salarié avant l'entrée en vigueur", auteur);
        }
        e.setActif(false);
        changerStatut(e, "SORTI");
        utilisateurs.findByIdEmploye(e.getId()).ifPresent(u -> {
            u.setActif(false);
            utilisateurs.save(u);
        });
        carriere.tracer(e, "SORTIE", dateSortie, "Sortie de l'entreprise : " + e.getMotifSortie(),
                "EN_PREAVIS", "SORTI", "SALARIE", e.getId(), auteur);
        audit.tracer("SALARIE", e.getId(), "SORTIE", "EN_PREAVIS", "SORTI", e.getMotifSortie(), auteur);
        employes.validateursParRole("RH").forEach(rh -> notifications.notifier(rh.getId(), "SALARIE", e.getId(),
                e.getNomComplet() + " est sorti(e) des effectifs : dossier archivé en lecture seule."));
    }

    // ------------------------------------------------------------ utilitaires

    private EmployeModel charger(Integer id) {
        return employes.findById(id).orElseThrow(() -> new Bloc4Exception("Salarié introuvable."));
    }

    private void changerStatut(EmployeModel e, String statut) {
        e.setStatutAdministratif(statut);
        employes.save(e);
    }

    private java.util.Optional<ContratModel> contratEnVigueur(Integer idEmploye) {
        List<ContratModel> liste = contrats.findByIdEmployeAndStatutWorkflowInOrderByDateDebutDesc(
                idEmploye, List.of("EN_VIGUEUR"));
        return liste.stream().findFirst();
    }

    private java.util.Optional<AffectationModel> affectationActive(Integer idEmploye) {
        return affectations.findFirstByIdEmployeAndStatutOrderByDateDebutDesc(idEmploye, "ACTIVE");
    }

    private static void exigerTexte(String valeur, String message) {
        if (valeur == null || valeur.isBlank()) {
            throw new Bloc4Exception(message);
        }
    }
}
