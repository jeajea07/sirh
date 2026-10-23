package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.AffectationModel;
import com.gestionrh.sirh.model.ContratModel;
import com.gestionrh.sirh.model.DepartementModel;
import com.gestionrh.sirh.model.EmployeModel;
import com.gestionrh.sirh.model.PosteModel;
import com.gestionrh.sirh.model.SignatureContratModel;
import com.gestionrh.sirh.model.TypeContratModel;
import com.gestionrh.sirh.model.ValidationB4Model;
import com.gestionrh.sirh.repository.AffectationRepository;
import com.gestionrh.sirh.repository.ContratRepository;
import com.gestionrh.sirh.repository.DepartementRepository;
import com.gestionrh.sirh.repository.EmployeRepository;
import com.gestionrh.sirh.repository.PosteRepository;
import com.gestionrh.sirh.repository.SignatureContratRepository;
import com.gestionrh.sirh.repository.TypeContratRepository;
import com.gestionrh.sirh.repository.ValidationB4Repository;
import com.gestionrh.sirh.service.dto.ContratDetail;
import com.gestionrh.sirh.service.dto.ContratEcheance;
import com.gestionrh.sirh.service.dto.ContratForm;
import com.gestionrh.sirh.service.dto.ContratResume;
import com.gestionrh.sirh.service.dto.ContratVue;
import com.gestionrh.sirh.service.dto.DocumentResume;
import com.gestionrh.sirh.service.dto.EmbaucheDTO;
import com.gestionrh.sirh.service.dto.EvenementAuditVue;
import com.gestionrh.sirh.service.dto.FiltreContrat;
import com.gestionrh.sirh.service.dto.SignatureVue;
import com.gestionrh.sirh.service.dto.ValidationVue;
import com.gestionrh.sirh.util.Formats;

@Service
public class ContratService {

    private static final int DUREE_MAX_CDD_MOIS = 24;

    private final ContratRepository contrats;
    private final EmployeRepository employes;
    private final TypeContratRepository types;
    private final AffectationRepository affectations;
    private final SignatureContratRepository signatures;
    private final ValidationB4Repository validations;
    private final PosteRepository postes;
    private final DepartementRepository departements;
    private final DocumentService documents;
    private final AuditService audit;
    private final NotificationService notifications;
    private final OnboardingService onboarding;

    public ContratService(ContratRepository contrats, EmployeRepository employes,
                          TypeContratRepository types, AffectationRepository affectations,
                          SignatureContratRepository signatures, ValidationB4Repository validations,
                          PosteRepository postes, DepartementRepository departements,
                          DocumentService documents, AuditService audit,
                          NotificationService notifications, OnboardingService onboarding) {
        this.contrats = contrats;
        this.employes = employes;
        this.types = types;
        this.affectations = affectations;
        this.signatures = signatures;
        this.validations = validations;
        this.postes = postes;
        this.departements = departements;
        this.documents = documents;
        this.audit = audit;
        this.notifications = notifications;
        this.onboarding = onboarding;
    }

    @Transactional(readOnly = true)
    public Page<ContratResume> lister(FiltreContrat filtre) {
        Pageable pageable = filtre.toPageable();
        Page<ContratModel> page;
        if (filtre.getRecherche() != null && !filtre.getRecherche().isBlank()) {
            page = contrats.rechercher(filtre.getRecherche().trim(), pageable);
        } else if (filtre.getStatut() != null && !filtre.getStatut().isBlank()) {
            page = contrats.findByStatutWorkflow(filtre.getStatut(), pageable);
        } else if (filtre.getTypeOperation() != null && !filtre.getTypeOperation().isBlank()) {
            page = contrats.findByTypeOperation(filtre.getTypeOperation(), pageable);
        } else {
            page = contrats.findAll(pageable);
        }
        return page.map(this::resume);
    }

    @Transactional(readOnly = true)
    public List<ContratResume> listerParEmploye(Integer idEmploye) {
        return contrats.findByIdEmployeOrderByDateDebutDesc(idEmploye).stream().map(this::resume).toList();
    }

    @Transactional(readOnly = true)
    public ContratModel charger(Integer id) {
        return contrats.findAvecRelationsById(id)
                .orElseThrow(() -> new Bloc4Exception("Contrat introuvable."));
    }

    @Transactional(readOnly = true)
    public ContratDetail detail(Integer id) {
        ContratModel c = charger(id);
        ContratDetail d = new ContratDetail();
        d.setContrat(vue(c));
        d.setSignatures(signatures.findByContrat_IdOrderByDateSignatureAsc(id).stream()
                .map(s -> new SignatureVue(s.getId(), s.getPartieLibelle(), s.getNomSignataire(),
                        Formats.date(s.getDateSignature()), s.getStatutLibelle(), s.getStatutBadge(),
                        s.getCommentaire()))
                .toList());
        List<DocumentResume> docs = documents.listerParContrat(id);
        d.setDocuments(docs);
        d.setAvenants(contrats.findByContratPrecedent_IdOrderByVersionDesc(id).stream()
                .map(this::resume).toList());
        d.setHistorique(audit.historique("CONTRAT", id).stream()
                .map(e -> new EvenementAuditVue(e.getId(), e.getAction(), e.getAncienneValeur(),
                        e.getNouvelleValeur(), e.getCommentaire(), e.getAuteur().getNomComplet(),
                        Formats.dateHeure(e.getDateEvenement())))
                .toList());
        d.setValidations(validations.findByTypeObjetAndIdObjetOrderByIdAsc("CONTRAT", id).stream()
                .map(v -> new ValidationVue(v.getId(), v.getEtapeLibelle(),
                        v.getValidateur() == null ? "—" : v.getValidateur().getNomComplet(),
                        v.getDecisionLibelle(), v.getDecisionBadge(), v.getCommentaire(),
                        Formats.dateHeure(v.getDateReception()), Formats.dateHeure(v.getDateDecision())))
                .toList());
        d.setPeutSoumettre("BROUILLON".equals(c.getStatutWorkflow()));
        d.setPeutValider("A_VALIDER".equals(c.getStatutWorkflow()));
        d.setPeutSigner("A_SIGNER".equals(c.getStatutWorkflow()));
        d.setPeutActiver("SIGNE".equals(c.getStatutWorkflow()) && c.isActivable());
        d.setPeutTerminer("EN_VIGUEUR".equals(c.getStatutWorkflow()));
        d.setPeutAnnuler(!"EN_VIGUEUR".equals(c.getStatutWorkflow())
                && !"TERMINE".equals(c.getStatutWorkflow())
                && !"ANNULE".equals(c.getStatutWorkflow()));
        d.setPeutCreerAvenant("EN_VIGUEUR".equals(c.getStatutWorkflow()));
        d.setPeutRenouveler("EN_VIGUEUR".equals(c.getStatutWorkflow())
                && c.getTypeContrat() != null && c.getTypeContrat().isAvecDuree());
        return d;
    }

    @Transactional(readOnly = true)
    public ContratForm formulaireNouveau(Integer idEmploye) {
        ContratForm f = new ContratForm();
        f.setIdEmploye(idEmploye);
        f.setDateDebut(LocalDate.now());
        f.setTypeOperation("INITIAL");
        return f;
    }

    @Transactional(readOnly = true)
    public ContratForm formulaireDe(Integer id) {
        ContratModel c = charger(id);
        if (!c.isModifiable()) {
            throw new Bloc4Exception("Seul un contrat en brouillon peut être modifié.");
        }
        return formulaireCopie(c);
    }

    @Transactional(readOnly = true)
    public ContratForm formulaireCopie(Integer id) {
        return formulaireCopie(charger(id));
    }

    private ContratForm formulaireCopie(ContratModel c) {
        ContratForm f = new ContratForm();
        f.setId(c.getId());
        f.setIdEmploye(c.getEmploye().getId());
        f.setIdTypeContrat(c.getTypeContrat().getId());
        f.setTypeOperation(c.getTypeOperation());
        f.setDateSignature(c.getDateSignature());
        f.setDateDebut(c.getDateDebut());
        f.setDateFin(c.getDateFin());
        f.setFonction(c.getFonction());
        f.setCategorieProfessionnelle(c.getCategorieProfessionnelle());
        f.setSalaire(c.getSalaire());
        f.setMotifRecours(c.getMotifRecours());
        f.setCommentaire(c.getCommentaire());
        f.setVersion(c.getVersion());
        return f;
    }

    public List<String> verifier(ContratForm f, boolean creation) {
        return verifier(f, creation, null);
    }

    private List<String> verifier(ContratForm f, boolean creation, ContratModel precedentFourni) {
        List<String> erreurs = new ArrayList<>();
        EmployeModel employe = f.getIdEmploye() == null ? null
                : employes.findById(f.getIdEmploye()).orElse(null);
        if (employe == null || !employe.isActif()) {
            erreurs.add("Le salarié doit exister et être actif.");
        }
        TypeContratModel type = f.getIdTypeContrat() == null ? null
                : types.findById(f.getIdTypeContrat()).orElse(null);
        if (type == null) {
            erreurs.add("Le type de contrat est obligatoire.");
        }
        if (f.getFonction() == null || f.getFonction().isBlank()) {
            erreurs.add("La fonction est obligatoire.");
        }
        if (f.getSalaire() == null || f.getSalaire().compareTo(BigDecimal.ZERO) <= 0) {
            erreurs.add("Le salaire doit être supérieur à zéro.");
        }
        if (f.getDateDebut() == null) {
            erreurs.add("La date de début est obligatoire.");
        }
        if (f.getDateSignature() != null && f.getDateSignature().isAfter(LocalDate.now())) {
            erreurs.add("La date de signature ne peut pas être dans le futur.");
        }
        if (type != null && "CDI".equalsIgnoreCase(type.getCode())) {
            f.setDateFin(null);
        } else if (type != null && type.isAvecDuree()) {
            if (f.getDateFin() == null) {
                erreurs.add("La date de fin est obligatoire pour ce type de contrat.");
            } else if (f.getDateDebut() != null && !f.getDateFin().isAfter(f.getDateDebut())) {
                erreurs.add("La date de fin doit être postérieure à la date de début.");
            }
        }
        if ("RENOUVELLEMENT".equals(f.getTypeOperation())) {
            ContratModel precedent = precedentFourni != null ? precedentFourni
                    : (f.getId() == null ? null : contrats.findById(f.getId()).orElse(null));
            if (precedent == null) {
                erreurs.add("Le contrat précédent est obligatoire pour un renouvellement.");
            } else {
                if (precedent.getTypeContrat() == null || !precedent.getTypeContrat().isAvecDuree()) {
                    erreurs.add("Seul un contrat à durée déterminée peut être renouvelé.");
                }
                if (precedent.getDateFin() != null && f.getDateDebut() != null
                        && !f.getDateDebut().isAfter(precedent.getDateFin())) {
                    erreurs.add("La nouvelle date de début doit être postérieure à la fin du contrat précédent.");
                }
                long total = dureeTotale(precedent) + (f.getDateDebut() == null || f.getDateFin() == null ? 0
                        : ChronoUnit.MONTHS.between(f.getDateDebut(), f.getDateFin()));
                if (total > DUREE_MAX_CDD_MOIS) {
                    erreurs.add("La durée totale des contrats successifs dépasse " + DUREE_MAX_CDD_MOIS + " mois.");
                }
            }
        }
        if ("AVENANT".equals(f.getTypeOperation())) {
            ContratModel precedent = precedentFourni != null ? precedentFourni
                    : (f.getId() == null ? null : contrats.findById(f.getId()).orElse(null));
            if (precedent == null || !"EN_VIGUEUR".equals(precedent.getStatutWorkflow())) {
                erreurs.add("Un avenant nécessite un contrat en vigueur.");
            }
        }
        if (precedentFourni == null && employe != null && !"RENOUVELLEMENT".equals(f.getTypeOperation())
                && !"AVENANT".equals(f.getTypeOperation())
                && contrats.existsByIdEmployeAndStatutWorkflowIn(employe.getId(),
                        List.of("EN_VIGUEUR", "SIGNE"))) {
            if (f.getId() == null || !contrats.findById(f.getId()).map(c ->
                    "BROUILLON".equals(c.getStatutWorkflow())).orElse(false)) {
                erreurs.add("Ce salarié possède déjà un contrat actif ou signé.");
            }
        }
        if (creation && f.getId() == null) {
            // la référence est générée par le service, aucune collision possible à ce stade
        }
        return erreurs;
    }

    @Transactional
    public ContratModel enregistrer(ContratForm f, EmployeModel auteur) {
        if (f.getId() == null && ("AVENANT".equals(f.getTypeOperation())
                || "RENOUVELLEMENT".equals(f.getTypeOperation()))) {
            throw new Bloc4Exception("Utilisez l'action dédiée pour créer un avenant ou un renouvellement.");
        }
        return enregistrerInterne(f, auteur, null, f.getTypeOperation());
    }

    @Transactional
    public void soumettre(Integer id, EmployeModel auteur) {
        ContratModel c = charger(id);
        if (!c.isSoumettable()) {
            throw new Bloc4Exception("Seul un contrat en brouillon peut être soumis.");
        }
        String avant = c.getStatutWorkflow();
        c.setStatutWorkflow("A_VALIDER");
        c.setDateMaj(LocalDateTime.now());
        contrats.save(c);

        ValidationB4Model v = new ValidationB4Model();
        v.setTypeObjet("CONTRAT");
        v.setIdObjet(c.getId());
        v.setEtape("VERIFICATION_RH");
        v.setDateReception(LocalDateTime.now());
        validations.save(v);

        notifierRh("CONTRAT", c.getId(), "Contrat " + c.getReference() + " à valider.");
        audit.tracer("CONTRAT", c.getId(), "SOUMISSION", avant, "A_VALIDER", null, auteur);
    }

    @Transactional
    public void valider(Integer id, String commentaire, EmployeModel validateur) {
        ContratModel c = charger(id);
        if (!"A_VALIDER".equals(c.getStatutWorkflow())) {
            throw new Bloc4Exception("Seul un contrat à valider peut être validé.");
        }
        c.setStatutWorkflow("A_SIGNER");
        c.setDateMaj(LocalDateTime.now());
        contrats.save(c);
        fermerValidation(c.getId(), "VALIDEE", commentaire, validateur);
        if (c.getCreePar() != null) {
            notifications.notifier(c.getCreePar().getId(), "CONTRAT", c.getId(),
                    "Contrat " + c.getReference() + " validé, en attente de signature.");
        }
        audit.tracer("CONTRAT", c.getId(), "VALIDATION", "A_VALIDER", "A_SIGNER", commentaire, validateur);
    }

    @Transactional
    public void renvoyer(Integer id, String commentaire, EmployeModel validateur) {
        if (commentaire == null || commentaire.isBlank()) {
            throw new Bloc4Exception("Un commentaire est obligatoire pour renvoyer le contrat.");
        }
        ContratModel c = charger(id);
        if (!"A_VALIDER".equals(c.getStatutWorkflow())) {
            throw new Bloc4Exception("Seul un contrat à valider peut être renvoyé.");
        }
        c.setStatutWorkflow("BROUILLON");
        c.setDateMaj(LocalDateTime.now());
        contrats.save(c);
        fermerValidation(c.getId(), "RENVOYEE", commentaire, validateur);
        if (c.getCreePar() != null) {
            notifications.notifier(c.getCreePar().getId(), "CONTRAT", c.getId(),
                    "Contrat " + c.getReference() + " renvoyé : " + commentaire);
        }
        audit.tracer("CONTRAT", c.getId(), "RENVOI", "A_VALIDER", "BROUILLON", commentaire, validateur);
    }

    @Transactional
    public void signer(Integer id, String partie, String nomSignataire,
                       LocalDate dateSignature, EmployeModel auteur) {
        if (!"SALARIE".equals(partie) && !"ENTREPRISE".equals(partie)) {
            throw new Bloc4Exception("La partie signataire est invalide.");
        }
        if (nomSignataire == null || nomSignataire.isBlank() || dateSignature == null) {
            throw new Bloc4Exception("Le nom du signataire et la date de signature sont obligatoires.");
        }
        if (dateSignature.isAfter(LocalDate.now())) {
            throw new Bloc4Exception("La signature ne peut pas être datée dans le futur.");
        }
        ContratModel c = charger(id);
        if (!c.isSignable()) {
            throw new Bloc4Exception("Seul un contrat à signer peut être signé.");
        }
        SignatureContratModel s = signatures.findByIdContratAndPartie(id, partie)
                .orElseGet(SignatureContratModel::new);
        s.setContrat(c);
        s.setPartie(partie);
        s.setNomSignataire(nomSignataire.trim());
        s.setDateSignature(dateSignature);
        s.setStatut("SIGNEE");
        s.setDateCreation(s.getId() == null ? LocalDateTime.now() : s.getDateCreation());
        signatures.save(s);

        if (signatures.countByIdContratAndStatut(id, "SIGNEE") >= 2
                || (signatures.existsByIdContratAndPartieAndStatut(id, "SALARIE", "SIGNEE")
                    && signatures.existsByIdContratAndPartieAndStatut(id, "ENTREPRISE", "SIGNEE"))) {
            c.setStatutWorkflow("SIGNE");
            c.setDateSignature(signatures.findByContrat_IdOrderByDateSignatureAsc(id).stream()
                    .map(SignatureContratModel::getDateSignature)
                    .filter(java.util.Objects::nonNull)
                    .max(LocalDate::compareTo).orElse(dateSignature));
            c.setDateMaj(LocalDateTime.now());
            contrats.save(c);
        }
        notifierRh("CONTRAT", id, "Contrat " + c.getReference() + " : signature " + partie.toLowerCase() + " enregistrée.");
        audit.tracer("CONTRAT", id, "SIGNATURE_" + partie, null, dateSignature.toString(), nomSignataire, auteur);
    }

    @Transactional
    public void activer(Integer id, EmployeModel auteur) {
        ContratModel c = charger(id);
        if (!"SIGNE".equals(c.getStatutWorkflow())) {
            throw new Bloc4Exception("Seul un contrat signé peut être activé.");
        }
        if (c.getDateDebut() != null && c.getDateDebut().isAfter(LocalDate.now())) {
            throw new Bloc4Exception("Le contrat n'est pas encore arrivé à sa date d'effet.");
        }
        c.setStatutWorkflow("EN_VIGUEUR");
        c.setStatutContractuel("ACTIF");
        c.setDateMaj(LocalDateTime.now());
        contrats.save(c);

        if (c.getContratPrecedent() != null) {
            ContratModel precedent = c.getContratPrecedent();
            if ("RENOUVELLEMENT".equals(c.getTypeOperation())) {
                precedent.setStatutWorkflow("TERMINE");
                precedent.setStatutContractuel("TERMINE");
                if (c.getDateDebut() != null) {
                    precedent.setDateFin(c.getDateDebut().minusDays(1));
                }
                precedent.setDateMaj(LocalDateTime.now());
                contrats.save(precedent);
            } else if ("AVENANT".equals(c.getTypeOperation())) {
                precedent.setStatutContractuel("REMPLACE");
                precedent.setDateMaj(LocalDateTime.now());
                contrats.save(precedent);
            }
        }
        affectations.findFirstByIdEmployeAndStatutOrderByDateDebutDesc(c.getEmploye().getId(), "ACTIVE")
                .ifPresent(a -> {
                    a.setContrat(c);
                    a.setDateMaj(LocalDateTime.now());
                    affectations.save(a);
                });
        notifierRh("CONTRAT", id, "Contrat " + c.getReference() + " activé.");
        if (c.getEmploye().getId() != null) {
            notifications.notifier(c.getEmploye().getId(), "CONTRAT", id,
                    "Votre contrat " + c.getReference() + " est désormais en vigueur.");
        }
        audit.tracer("CONTRAT", id, "ACTIVATION", "SIGNE", "EN_VIGUEUR", null, auteur);
    }

    @Transactional
    public void terminer(Integer id, String motif, LocalDate dateFin, EmployeModel auteur) {
        if (motif == null || motif.isBlank() || dateFin == null) {
            throw new Bloc4Exception("Le motif et la date de fin sont obligatoires.");
        }
        ContratModel c = charger(id);
        if (!c.isTerminable()) {
            throw new Bloc4Exception("Seul un contrat en vigueur peut être terminé.");
        }
        if (c.getDateDebut() != null && dateFin.isBefore(c.getDateDebut())) {
            throw new Bloc4Exception("La date de fin ne peut pas précéder la date de début.");
        }
        c.setDateFin(dateFin);
        c.setStatutWorkflow("TERMINE");
        c.setStatutContractuel("TERMINE");
        c.setDateMaj(LocalDateTime.now());
        contrats.save(c);
        affectations.findFirstByIdEmployeAndStatutOrderByDateDebutDesc(c.getEmploye().getId(), "ACTIVE")
                .ifPresent(a -> {
                    a.setStatut("TERMINEE");
                    a.setDateFin(dateFin);
                    a.setDateMaj(LocalDateTime.now());
                    affectations.save(a);
                });
        notifierRh("CONTRAT", id, "Contrat " + c.getReference() + " terminé.");
        audit.tracer("CONTRAT", id, "TERMINAISON", "EN_VIGUEUR", "TERMINE", motif, auteur);
    }

    @Transactional
    public void annuler(Integer id, String motif, EmployeModel auteur) {
        if (motif == null || motif.isBlank()) {
            throw new Bloc4Exception("Le motif d'annulation est obligatoire.");
        }
        ContratModel c = charger(id);
        if ("EN_VIGUEUR".equals(c.getStatutWorkflow())) {
            throw new Bloc4Exception("Un contrat en vigueur doit être terminé, pas annulé.");
        }
        String avant = c.getStatutWorkflow();
        c.setStatutWorkflow("ANNULE");
        c.setStatutContractuel("TERMINE");
        c.setDateMaj(LocalDateTime.now());
        contrats.save(c);
        notifierRh("CONTRAT", id, "Contrat " + c.getReference() + " annulé.");
        audit.tracer("CONTRAT", id, "ANNULATION", avant, "ANNULE", motif, auteur);
    }

    @Transactional
    public ContratModel creerAvenant(Integer idContrat, ContratForm f, EmployeModel auteur) {
        ContratModel origine = charger(idContrat);
        if (!"EN_VIGUEUR".equals(origine.getStatutWorkflow())) {
            throw new Bloc4Exception("Un avenant ne peut être créé que depuis un contrat en vigueur.");
        }
        f.setId(null);
        f.setIdEmploye(origine.getEmploye().getId());
        f.setTypeOperation("AVENANT");
        if (f.getIdTypeContrat() == null) f.setIdTypeContrat(origine.getTypeContrat().getId());
        if (f.getFonction() == null || f.getFonction().isBlank()) f.setFonction(origine.getFonction());
        if (f.getSalaire() == null) f.setSalaire(origine.getSalaire());
        if (f.getDateDebut() == null) f.setDateDebut(LocalDate.now());
        f.setDateFin(origine.getDateFin());
        return enregistrerInterne(f, auteur, origine, "AVENANT");
    }

    @Transactional
    public ContratModel renouveler(Integer idContrat, ContratForm f, EmployeModel auteur) {
        ContratModel origine = charger(idContrat);
        if (origine.getTypeContrat() == null || !origine.getTypeContrat().isAvecDuree()) {
            throw new Bloc4Exception("Seul un contrat à durée déterminée peut être renouvelé.");
        }
        if (!"EN_VIGUEUR".equals(origine.getStatutWorkflow()) && !"TERMINE".equals(origine.getStatutWorkflow())) {
            throw new Bloc4Exception("Le contrat d'origine doit être en vigueur ou terminé.");
        }
        if (f.getDateDebut() == null || (origine.getDateFin() != null
                && !f.getDateDebut().isAfter(origine.getDateFin()))) {
            throw new Bloc4Exception("La nouvelle date de début doit être postérieure à la fin du contrat précédent.");
        }
        long duree = dureeTotale(origine);
        if (f.getDateFin() != null) {
            duree += ChronoUnit.MONTHS.between(f.getDateDebut(), f.getDateFin());
        }
        if (duree > DUREE_MAX_CDD_MOIS) {
            throw new Bloc4Exception("La durée totale des contrats successifs dépasse "
                    + DUREE_MAX_CDD_MOIS + " mois.");
        }
        f.setId(null);
        f.setIdEmploye(origine.getEmploye().getId());
        f.setTypeOperation("RENOUVELLEMENT");
        if (f.getIdTypeContrat() == null) f.setIdTypeContrat(origine.getTypeContrat().getId());
        if (f.getFonction() == null || f.getFonction().isBlank()) f.setFonction(origine.getFonction());
        if (f.getSalaire() == null) f.setSalaire(origine.getSalaire());
        return enregistrerInterne(f, auteur, origine, "RENOUVELLEMENT");
    }

    @Transactional
    public ContratModel creerDepuisEmbauche(EmbaucheDTO dto, EmployeModel auteur) {
        if (dto.getIdRecrutement() != null) {
            Optional<ContratModel> existant = contrats.findByIdRecrutementExterne(dto.getIdRecrutement());
            if (existant.isPresent()) {
                return existant.get();
            }
        }
        if (dto.getIdPoste() == null || dto.getIdDepartement() == null) {
            throw new Bloc4Exception("Département et poste requis pour l'embauche : compléter dans le Bloc 3 "
                    + "ou créer le contrat manuellement.");
        }
        if (dto.getCodeTypeContrat() == null || "AUTRE".equalsIgnoreCase(dto.getCodeTypeContrat())) {
            throw new Bloc4Exception("Type de contrat non reconnu : faire choisir la RH.");
        }
        if (dto.getDateDebut() == null) {
            throw new Bloc4Exception("La date de début du contrat est obligatoire.");
        }
        if (dto.getSalaire() == null || dto.getSalaire().signum() <= 0) {
            throw new Bloc4Exception("Le salaire mensuel doit être strictement positif.");
        }
        if (dto.getNom() == null || dto.getNom().isBlank()
                || dto.getPrenom() == null || dto.getPrenom().isBlank()) {
            throw new Bloc4Exception("Le nom et le prénom du salarié sont obligatoires.");
        }
        TypeContratModel type = types.findByCode(dto.getCodeTypeContrat())
                .orElseThrow(() -> new Bloc4Exception("Type de contrat inconnu : " + dto.getCodeTypeContrat()));
        PosteModel poste = postes.findById(dto.getIdPoste())
                .orElseThrow(() -> new Bloc4Exception("Poste introuvable."));
        DepartementModel departement = departements.findById(dto.getIdDepartement())
                .orElseThrow(() -> new Bloc4Exception("Département introuvable."));
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new Bloc4Exception("L'email du salarié est obligatoire.");
        }

        EmployeModel employe = employes.findByEmailIgnoreCase(dto.getEmail().trim()).orElse(null);
        if (employe == null) {
            if (dto.getCin() != null && !dto.getCin().isBlank()) {
                Optional<EmployeModel> parCin = employes.findByCin(dto.getCin().trim());
                if (parCin.isPresent()) {
                    throw new Bloc4Exception("Le CIN est déjà utilisé par un autre salarié.");
                }
            }
            employe = new EmployeModel();
            employe.setMatricule("EMP" + String.format("%03d", employes.maxId() + 1));
            employe.setNom(dto.getNom());
            employe.setPrenom(dto.getPrenom());
            employe.setEmail(dto.getEmail().trim());
            employe.setTelephone(dto.getTelephone());
            employe.setCin(dto.getCin());
            employe.setAdresse(dto.getAdresse());
            employe.setDateNaissance(dto.getDateNaissance());
            employe.setDateEmbauche(dto.getDateDebut());
            employe.setPoste(poste);
            employe.setDepartement(departement);
            employe.setActif(true);
            employe = employes.save(employe);
        }

        ContratModel c = new ContratModel();
        c.setEmploye(employe);
        c.setReference(genererReference());
        c.setTypeContrat(type);
        c.setTypeOperation("INITIAL");
        c.setDateDebut(dto.getDateDebut());
        if (type.isAvecDuree() && dto.getDureeMois() != null) {
            c.setDateFin(dto.getDateDebut().plusMonths(dto.getDureeMois()));
        }
        c.setFonction(dto.getFonction());
        c.setCategorieProfessionnelle(dto.getCategorieProfessionnelle());
        c.setSalaire(dto.getSalaire());
        c.setStatutWorkflow("BROUILLON");
        c.setStatutContractuel("NON_ACTIF");
        c.setIdRecrutementExterne(dto.getIdRecrutement());
        c.setMotifRecours(dto.getMotifRecours());
        c.setVersion(1);
        c.setDateCreation(LocalDateTime.now());
        c.setDateMaj(LocalDateTime.now());
        c.setCreePar(auteur);
        c = contrats.save(c);

        AffectationModel a = new AffectationModel();
        a.setEmploye(employe);
        a.setPoste(poste);
        a.setDepartement(departement);
        a.setContrat(c);
        a.setDateDebut(dto.getDateDebut());
        a.setMotif("Embauche issue du recrutement");
        a.setStatut("ACTIVE");
        a.setDateCreation(LocalDateTime.now());
        a.setDateMaj(LocalDateTime.now());
        a.setCreePar(auteur);
        affectations.save(a);

        onboarding.creerAutomatique(c.getId(), auteur).ifPresent(o -> {
            // l'onboarding est généré, aucune action supplémentaire
        });
        if (onboardingExistePas(c.getId())) {
            notifierRh("CONTRAT", c.getId(), "Aucun modèle d'onboarding applicable au contrat "
                    + c.getReference() + ".");
        }
        audit.tracer("CONTRAT", c.getId(), "CREATION_DEPUIS_EMBAUCHE", null, c.getReference(),
                "Recrutement " + dto.getIdRecrutement(), auteur);
        return c;
    }

    private boolean onboardingExistePas(Integer idContrat) {
        return onboarding.listerParContrat(idContrat).isEmpty();
    }

    @Transactional(readOnly = true)
    public List<ContratEcheance> echeances(int jours) {
        LocalDate aujourdhui = LocalDate.now();
        return contrats.echeances(aujourdhui, aujourdhui.plusDays(Math.max(jours, 0))).stream()
                .map(c -> new ContratEcheance(resume(c), Formats.joursRestants(c.getDateFin())))
                .toList();
    }

    public String genererReference() {
        String prefixe = "CTR-" + LocalDate.now().getYear() + "-";
        int numero = contrats.findFirstByReferenceStartingWithOrderByReferenceDesc(prefixe)
                .map(c -> c.getReference().substring(prefixe.length()))
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }).orElse(0) + 1;
        String reference = prefixe + String.format("%04d", numero);
        if (contrats.findFirstByReferenceStartingWithOrderByReferenceDesc(reference).isPresent()) {
            reference = prefixe + String.format("%04d", numero + 1);
        }
        return reference;
    }

    private ContratModel enregistrerInterne(ContratForm f, EmployeModel auteur,
                                            ContratModel precedent, String operation) {
        List<String> erreurs = verifier(f, f.getId() == null, precedent);
        if (!erreurs.isEmpty()) {
            throw new Bloc4Exception(String.join(" ", erreurs));
        }
        ContratModel c;
        if (f.getId() == null) {
            c = new ContratModel();
            c.setReference(genererReference());
            c.setVersion(1);
            c.setStatutWorkflow("BROUILLON");
            c.setStatutContractuel("NON_ACTIF");
            c.setDateCreation(LocalDateTime.now());
            c.setCreePar(auteur);
            c.setTypeOperation(operation == null ? "INITIAL" : operation);
            c.setContratPrecedent(precedent);
            if (precedent != null) {
                c.setVersion((precedent.getVersion() == null ? 1 : precedent.getVersion()) + 1);
            }
        } else {
            c = charger(f.getId());
            if (!c.isModifiable()) {
                throw new Bloc4Exception("Seul un contrat en brouillon peut être modifié.");
            }
        }
        c.setEmploye(employes.findById(f.getIdEmploye())
                .orElseThrow(() -> new Bloc4Exception("Salarié introuvable.")));
        c.setTypeContrat(types.findById(f.getIdTypeContrat())
                .orElseThrow(() -> new Bloc4Exception("Type de contrat introuvable.")));
        c.setDateSignature(f.getDateSignature());
        c.setDateDebut(f.getDateDebut());
        c.setDateFin(f.getDateFin());
        c.setFonction(f.getFonction());
        c.setCategorieProfessionnelle(f.getCategorieProfessionnelle());
        c.setSalaire(f.getSalaire());
        c.setMotifRecours(f.getMotifRecours());
        c.setCommentaire(f.getCommentaire());
        c.setDateMaj(LocalDateTime.now());
        c = contrats.save(c);
        audit.tracer("CONTRAT", c.getId(), f.getId() == null ? "CREATION" : "MODIFICATION",
                null, c.getReference(), operation, auteur);
        return c;
    }

    private void fermerValidation(Integer idContrat, String decision, String commentaire,
                                  EmployeModel validateur) {
        validations.findFirstByTypeObjetAndIdObjetAndDecisionIsNullOrderByIdDesc("CONTRAT", idContrat)
                .ifPresent(v -> {
                    v.setDecision(decision);
                    v.setCommentaire(commentaire);
                    v.setValidateur(validateur);
                    v.setDateDecision(LocalDateTime.now());
                    validations.save(v);
                });
    }

    private ContratResume resume(ContratModel c) {
        return new ContratResume(c.getId(), c.getReference(), c.getEmploye().getNomComplet(),
                c.getTypeContrat() == null ? "—" : c.getTypeContrat().getLibelle(),
                c.getDateDebut(), c.getDateFin(), c.getStatutWorkflow(), c.getStatutBadge(),
                Formats.montant(c.getSalaire()), Formats.joursRestants(c.getDateFin()),
                c.getTypeOperationLibelle());
    }

    private ContratVue vue(ContratModel c) {
        ContratVue v = new ContratVue();
        v.setId(c.getId());
        v.setReference(c.getReference());
        v.setIdEmploye(c.getEmploye().getId());
        v.setEmployeNom(c.getEmploye().getNomComplet());
        v.setEmployeMatricule(c.getEmploye().getMatricule());
        v.setEmployeEmail(c.getEmploye().getEmail());
        v.setTypeContratLibelle(c.getTypeContrat() == null ? "—" : c.getTypeContrat().getLibelle());
        v.setTypeOperationLibelle(c.getTypeOperationLibelle());
        v.setDateSignatureFr(Formats.date(c.getDateSignature()));
        v.setDateDebutFr(Formats.date(c.getDateDebut()));
        v.setDateFinFr(Formats.date(c.getDateFin()));
        v.setFonction(c.getFonction());
        v.setCategorieProfessionnelle(c.getCategorieProfessionnelle());
        v.setSalaireFr(Formats.montant(c.getSalaire()));
        v.setStatutWorkflow(c.getStatutWorkflow());
        v.setStatutLibelle(c.getStatutLibelle());
        v.setStatutBadge(c.getStatutBadge());
        v.setStatutContractuelLibelle(c.getStatutContractuelLibelle());
        v.setMotifRecours(c.getMotifRecours());
        v.setCommentaire(c.getCommentaire());
        v.setVersion(c.getVersion());
        long jours = c.getDateFin() == null ? -1 : Formats.joursRestants(c.getDateFin());
        v.setJoursRestants(jours);
        v.setDureeMois(c.getDureeMois());
        v.setContratPrecedentReference(c.getContratPrecedent() == null ? null
                : c.getContratPrecedent().getReference());
        return v;
    }

    private void notifierRh(String type, Integer id, String message) {
        employes.validateursParRole("RH").forEach(rh -> notifications.notifier(rh.getId(), type, id, message));
    }

    private static long dureeTotale(ContratModel contrat) {
        if (contrat.getDateDebut() == null || contrat.getDateFin() == null) {
            return 0;
        }
        return ChronoUnit.MONTHS.between(contrat.getDateDebut(), contrat.getDateFin());
    }
}
