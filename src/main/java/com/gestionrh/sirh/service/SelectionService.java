// Bloc 3
package com.gestionrh.sirh.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gestionrh.sirh.model.EtatDossierModel;
import com.gestionrh.sirh.model.EtatPropositionModel;
import com.gestionrh.sirh.model.EtatRecrutementModel;
import com.gestionrh.sirh.model.EtatTransfertModel;
import com.gestionrh.sirh.model.EtatValidationModel;
import com.gestionrh.sirh.model.HistoriqueModel;
import com.gestionrh.sirh.model.PropositionModel;
import com.gestionrh.sirh.model.RecrutementModel;
import com.gestionrh.sirh.model.ReponseCandidatModel;
import com.gestionrh.sirh.model.SelectionModel;
import com.gestionrh.sirh.model.TypeContratEmbaucheModel;
import com.gestionrh.sirh.model.TypeDecisionModel;
import com.gestionrh.sirh.model.TypeReponseModel;
import com.gestionrh.sirh.repository.EtatDossierRepository;
import com.gestionrh.sirh.repository.EtatPropositionRepository;
import com.gestionrh.sirh.repository.EtatRecrutementRepository;
import com.gestionrh.sirh.repository.EtatTransfertRepository;
import com.gestionrh.sirh.repository.EtatValidationRepository;
import com.gestionrh.sirh.repository.HistoriqueRepository;
import com.gestionrh.sirh.repository.PropositionRepository;
import com.gestionrh.sirh.repository.RecrutementRepository;
import com.gestionrh.sirh.repository.ReponseCandidatRepository;
import com.gestionrh.sirh.repository.SelectionRepository;
import com.gestionrh.sirh.repository.TypeContratEmbaucheRepository;
import com.gestionrh.sirh.repository.TypeDecisionRepository;
import com.gestionrh.sirh.repository.TypeReponseRepository;

@Service
public class SelectionService {
    private final SelectionRepository selections;
    private final PropositionRepository propositions;
    private final ReponseCandidatRepository reponses;
    private final RecrutementRepository recrutements;
    private final HistoriqueRepository historiques;
    private final EtatDossierRepository etatsDossier;
    private final TypeDecisionRepository decisions;
    private final EtatValidationRepository etatsValidation;
    private final EtatPropositionRepository etatsProposition;
    private final TypeReponseRepository typesReponse;
    private final TypeContratEmbaucheRepository typesContrat;
    private final EtatRecrutementRepository etatsRecrutement;
    private final EtatTransfertRepository etatsTransfert;

    public SelectionService(SelectionRepository selections,
                     PropositionRepository propositions,
                     ReponseCandidatRepository reponses,
                     RecrutementRepository recrutements,
                     HistoriqueRepository historiques,
                     EtatDossierRepository etatsDossier,
                     TypeDecisionRepository decisions,
                     EtatValidationRepository etatsValidation,
                     EtatPropositionRepository etatsProposition,
                     TypeReponseRepository typesReponse,
                     TypeContratEmbaucheRepository typesContrat,
                     EtatRecrutementRepository etatsRecrutement,
                     EtatTransfertRepository etatsTransfert) {
        this.selections = selections;
        this.propositions = propositions;
        this.reponses = reponses;
        this.recrutements = recrutements;
        this.historiques = historiques;
        this.etatsDossier = etatsDossier;
        this.decisions = decisions;
        this.etatsValidation = etatsValidation;
        this.etatsProposition = etatsProposition;
        this.typesReponse = typesReponse;
        this.typesContrat = typesContrat;
        this.etatsRecrutement = etatsRecrutement;
        this.etatsTransfert = etatsTransfert;
    }

    @Transactional(readOnly = true)
    public Page<SelectionModel> listerSelections(Integer idRecherche, Pageable pageable) {
        return selections.rechercher(idRecherche, pageable);
    }

    @Transactional(readOnly = true)
    public SelectionModel selection(Integer id) {
        return selections.findById(id).orElseThrow(() -> new Bloc3Exception("Dossier de sélection introuvable."));
    }

    @Transactional(readOnly = true)
    public List<PropositionModel> propositions(Integer idSelection) {
        return propositions.findBySelection_IdSelectionOrderByNumeroVersionDesc(idSelection);
    }

    @Transactional(readOnly = true)
    public List<ReponseCandidatModel> reponses(Integer idProposition) {
        return reponses.findByProposition_IdPropositionOrderByReponseLeDesc(idProposition);
    }

    @Transactional(readOnly = true)
    public RecrutementModel recrutement(Integer idSelection) {
        return recrutements.findBySelection_IdSelection(idSelection).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<HistoriqueModel> historique(Integer idSelection) {
        List<HistoriqueModel> resultat = new ArrayList<>(
                historiques.findByTypeElementAndIdElementOrderByDateActionDesc("SELECTION", idSelection));
        for (PropositionModel proposition : propositions
                .findBySelection_IdSelectionOrderByNumeroVersionDesc(idSelection)) {
            resultat.addAll(historiques.findByTypeElementAndIdElementOrderByDateActionDesc(
                    "PROPOSITION", proposition.getIdProposition()));
        }
        recrutements.findBySelection_IdSelection(idSelection).ifPresent(r -> resultat.addAll(
                historiques.findByTypeElementAndIdElementOrderByDateActionDesc(
                        "RECRUTEMENT", r.getIdRecrutement())));
        resultat.sort(Comparator.comparing(HistoriqueModel::getDateAction).reversed());
        return resultat;
    }

    @Transactional(readOnly = true)
    public Integer selectionPourProposition(Integer idProposition) {
        return propositions.findById(idProposition)
                .orElseThrow(() -> new Bloc3Exception("Proposition introuvable."))
                .getSelection().getIdSelection();
    }

    @Transactional(readOnly = true)
    public List<EtatDossierModel> etatsDossier() { return etatsDossier.findAllByOrderByLibelleAsc(); }

    @Transactional(readOnly = true)
    public List<TypeDecisionModel> decisions() { return decisions.findAllByOrderByLibelleAsc(); }

    @Transactional(readOnly = true)
    public List<TypeReponseModel> typesReponse() { return typesReponse.findAllByOrderByLibelleAsc(); }

    @Transactional(readOnly = true)
    public List<TypeContratEmbaucheModel> typesContrat() { return typesContrat.findAllByOrderByLibelleAsc(); }

    @Transactional
    public SelectionModel recevoirDossier(Integer idCandidature, Integer idOffre,
                                             Integer idCandidat, BigDecimal scoreTest,
                                             BigDecimal scoreEntretienRh, BigDecimal scoreEntretienTech,
                                             BigDecimal scoreEntretienDg, BigDecimal scoreFinal,
                                             Integer rangClassement, Integer auteur) {
        if (idCandidature == null || idCandidature < 1 || idOffre == null || idOffre < 1
                || idCandidat == null || idCandidat < 1) {
            throw new Bloc3Exception("Les identifiants de candidature, d'offre et de candidat sont obligatoires.");
        }
        verifierScore(scoreTest, 20, "test");
        verifierScore(scoreEntretienRh, 5, "entretien RH");
        verifierScore(scoreEntretienTech, 5, "entretien technique");
        verifierScore(scoreEntretienDg, 5, "entretien DG");
        verifierScore(scoreFinal, 5, "note finale");
        if (rangClassement != null && rangClassement < 1) {
            throw new Bloc3Exception("Le rang transmis doit être supérieur à zéro.");
        }
        if (selections.existsByIdCandidatureB2(idCandidature)) {
            throw new Bloc3Exception("Cette candidature du Bloc 2 a déjà été reçue.");
        }
        SelectionModel selection = new SelectionModel();
        selection.setIdCandidatureB2(idCandidature);
        selection.setIdOffreB2(idOffre);
        selection.setIdCandidatB2(idCandidat);
        selection.setScoreTest(scoreTest);
        selection.setScoreEntretienRh(scoreEntretienRh);
        selection.setScoreEntretienTech(scoreEntretienTech);
        selection.setScoreEntretienDg(scoreEntretienDg);
        selection.setScoreFinal(scoreFinal);
        selection.setRangClassement(rangClassement);
        selection.setEtatDossier(reference(etatsDossier.findByCode("A_VERIFIER").orElse(null), "état de dossier"));
        selection.setEtatValidation(etatValidation("NON_REQUISE"));
        SelectionModel recue = selections.save(selection);
        historiser("SELECTION", recue.getIdSelection(), "DOSSIER_RECU_BLOC2", auteur,
                "Références reçues : candidature " + idCandidature + ", offre " + idOffre + ", candidat " + idCandidat + ".");
        return recue;
    }

    @Transactional
    public void verifierDossier(Integer id, String codeEtat, String elementsManquants, Integer auteur) {
        SelectionModel selection = selection(id);
        EtatDossierModel etat = reference(etatsDossier.findByCode(codeEtat).orElse(null), "état de dossier");
        if (("INCOMPLET".equals(codeEtat) || "INCOHERENT".equals(codeEtat))
                && (elementsManquants == null || elementsManquants.isBlank())) {
            throw new Bloc3Exception("Indiquez les éléments manquants ou l'incohérence constatée.");
        }
        selection.setEtatDossier(etat);
        selection.setElementsManquants(elementsManquants == null || elementsManquants.isBlank() ? null : elementsManquants.trim());
        selections.save(selection);
        historiser("SELECTION", id, "VERIFICATION_DOSSIER", auteur,
                "État du dossier : " + etat.getLibelle());
    }

    @Transactional
    public void decider(Integer id, String codeDecision, String justification,
                        Integer rangAttente, boolean validationRequise, Integer auteur) {
        SelectionModel selection = selection(id);
        if (!"COMPLET".equals(selection.getEtatDossier().getCode())) {
            throw new Bloc3Exception("Le dossier doit être complet avant la décision finale.");
        }
        if (selection.getScoreFinal() == null) {
            throw new Bloc3Exception("La note finale transmise par le Bloc 2 doit être disponible avant la décision.");
        }
        TypeDecisionModel decision = reference(decisions.findByCode(codeDecision).orElse(null), "décision");
        if (validationRequise && !"RETENU".equals(codeDecision)) {
            throw new Bloc3Exception("La validation hiérarchique concerne une décision de recrutement.");
        }
        if (("NON_RETENU".equals(codeDecision) || "CLOTURE".equals(codeDecision))
                && (justification == null || justification.isBlank())) {
            throw new Bloc3Exception("Une justification est obligatoire pour cette décision.");
        }
        if ("LISTE_ATTENTE".equals(codeDecision) && (rangAttente == null || rangAttente < 1)) {
            throw new Bloc3Exception("La position dans la liste d'attente doit être supérieure à zéro.");
        }

        selection.setDecision(decision);
        selection.setJustification(justification == null || justification.isBlank() ? null : justification.trim());
        selection.setRangListeAttente("LISTE_ATTENTE".equals(codeDecision) ? rangAttente : null);
        selection.setDecidePar(auteur);
        selection.setDecideLe(LocalDate.now());
        selection.setValidationObligatoire(validationRequise);
        selection.setEtatValidation(etatValidation(validationRequise ? "EN_ATTENTE" : "NON_REQUISE"));
        selection.setValidePar(null);
        selection.setValideLe(null);
        selections.save(selection);
        historiser("SELECTION", id, "DECISION", auteur, "Décision enregistrée : " + decision.getLibelle());
    }

    @Transactional
    public void validerDecision(Integer id, boolean approuvee, Integer validateur, String commentaire) {
        SelectionModel selection = selection(id);
        if (selection.getDecision() == null || !"RETENU".equals(selection.getDecision().getCode())) {
            throw new Bloc3Exception("Seule une décision de recrutement peut être soumise à validation.");
        }
        if (!selection.isValidationObligatoire()) {
            throw new Bloc3Exception("Cette décision ne requiert pas de validation hiérarchique.");
        }
        if (!approuvee && (commentaire == null || commentaire.isBlank())) {
            throw new Bloc3Exception("Un commentaire est obligatoire pour refuser la validation.");
        }
        if (!"EN_ATTENTE".equals(selection.getEtatValidation().getCode())) {
            throw new Bloc3Exception("La décision n'est pas en attente de validation.");
        }
        String code = approuvee ? "VALIDEE" : "REFUSEE";
        selection.setEtatValidation(etatValidation(code));
        selection.setValidePar(validateur);
        selection.setValideLe(LocalDate.now());
        if (!approuvee && commentaire != null && !commentaire.isBlank()) {
            selection.setJustification(commentaire.trim());
        }
        selections.save(selection);
        historiser("SELECTION", id, "VALIDATION_DECISION", validateur,
                approuvee ? "Décision validée." : "Décision refusée. " + texte(commentaire));
    }

    @Transactional
    public PropositionModel preparerProposition(Integer idSelection, PropositionForm form, Integer auteur) {
        SelectionModel selection = selection(idSelection);
        verifierDecisionValidee(selection);
        if (form.getIntitulePoste() == null || form.getIntitulePoste().isBlank()
                || form.getSalaireMensuel() == null || form.getSalaireMensuel().signum() < 0
                || form.getDateDebutPrevue() == null || form.getIdTypeContrat() == null) {
            throw new Bloc3Exception("Complétez le poste, le contrat, le salaire et la date de début.");
        }
        TypeContratEmbaucheModel contrat = reference(typesContrat.findById(form.getIdTypeContrat()).orElse(null), "type de contrat");
        if ("CDD".equals(contrat.getCode()) && (form.getDureeMois() == null || form.getDureeMois() < 1)) {
            throw new Bloc3Exception("La durée en mois est obligatoire pour un CDD.");
        }
        if (form.getDateLimiteReponse() != null && form.getDateLimiteReponse().isBefore(LocalDate.now())) {
            throw new Bloc3Exception("La date limite de réponse ne peut pas être dans le passé.");
        }

        PropositionModel proposition = new PropositionModel();
        proposition.setSelection(selection);
        proposition.setNumeroVersion(propositions
                .findFirstBySelection_IdSelectionOrderByNumeroVersionDesc(idSelection)
                .map(p -> p.getNumeroVersion() + 1).orElse(1));
        proposition.setIntitulePoste(form.getIntitulePoste().trim());
        proposition.setTypeContrat(contrat);
        proposition.setSalaireMensuel(form.getSalaireMensuel());
        proposition.setDateDebutPrevue(form.getDateDebutPrevue());
        proposition.setDureeMois(form.getDureeMois());
        proposition.setEtatProposition(etatProposition("A_PREPARER"));
        proposition.setCreePar(auteur);
        proposition.setCreeLe(LocalDate.now());
        PropositionModel creee = propositions.save(proposition);
        historiser("PROPOSITION", creee.getIdProposition(), "PROPOSITION_PREPAREE", auteur,
                "Version " + creee.getNumeroVersion() + " préparée.");
        return creee;
    }

    @Transactional
    public void envoyerProposition(Integer idProposition, Integer auteur) {
        PropositionModel proposition = propositions.findById(idProposition)
                .orElseThrow(() -> new Bloc3Exception("Proposition introuvable."));
        verifierDecisionValidee(proposition.getSelection());
        if (!"A_PREPARER".equals(proposition.getEtatProposition().getCode())) {
            throw new Bloc3Exception("Cette proposition ne peut pas être envoyée dans son état actuel.");
        }
        if (proposition.getDateLimiteReponse() != null
                && !proposition.getDateLimiteReponse().isAfter(LocalDate.now())) {
            throw new Bloc3Exception("La date limite de réponse doit être postérieure à aujourd'hui.");
        }
        proposition.setEtatProposition(etatProposition("ENVOYEE"));
        proposition.setEnvoyeeLe(LocalDate.now());
        propositions.save(proposition);
        historiser("PROPOSITION", idProposition, "PROPOSITION_ENVOYEE", auteur,
                "Proposition envoyée au candidat (envoi à connecter au service de notification).");
    }

    @Transactional
    public void enregistrerReponse(Integer idProposition, String codeReponse, String commentaire, Integer auteur) {
        PropositionModel proposition = propositions.findById(idProposition)
                .orElseThrow(() -> new Bloc3Exception("Proposition introuvable."));
        if (!"ENVOYEE".equals(proposition.getEtatProposition().getCode())) {
            throw new Bloc3Exception("La proposition doit être envoyée avant d'enregistrer une réponse.");
        }
        TypeReponseModel type = reference(typesReponse.findByCode(codeReponse).orElse(null), "type de réponse");
        if (!"ACCEPTEE".equals(codeReponse) && (commentaire == null || commentaire.isBlank())) {
            throw new Bloc3Exception("Ajoutez un commentaire pour cette réponse.");
        }

        ReponseCandidatModel reponse = new ReponseCandidatModel();
        reponse.setProposition(proposition);
        reponse.setTypeReponse(type);
        reponse.setReponseLe(LocalDate.now());
        reponse.setCommentaire(texte(commentaire));
        reponses.save(reponse);

        String nouvelEtat = switch (codeReponse) {
            case "ACCEPTEE" -> "ACCEPTEE";
            case "REFUSEE" -> "REFUSEE";
            default -> "A_REVISER";
        };
        proposition.setEtatProposition(etatProposition(nouvelEtat));
        propositions.save(proposition);
        historiser("PROPOSITION", idProposition, "REPONSE_CANDIDAT", auteur,
                type.getLibelle() + (commentaire == null || commentaire.isBlank() ? "" : " : " + commentaire.trim()));
    }

    @Transactional
    public void confirmerRecrutement(Integer idSelection, Integer nombrePostes, Integer auteur) {
        SelectionModel selection = selection(idSelection);
        verifierDecisionValidee(selection);
        if (nombrePostes == null || nombrePostes < 1) {
            throw new Bloc3Exception("Le nombre de postes autorisés doit être supérieur à zéro.");
        }
        PropositionModel proposition = propositions
                .findFirstBySelection_IdSelectionOrderByNumeroVersionDesc(idSelection)
                .orElseThrow(() -> new Bloc3Exception("Préparez une proposition avant la confirmation."));
        if (!"ACCEPTEE".equals(proposition.getEtatProposition().getCode())
                || reponses(proposition.getIdProposition()).stream()
                    .noneMatch(r -> "ACCEPTEE".equals(r.getTypeReponse().getCode()))) {
            throw new Bloc3Exception("Le candidat doit avoir accepté la dernière proposition.");
        }
        if (recrutements.findBySelection_IdSelection(idSelection).isPresent()) {
            throw new Bloc3Exception("Le recrutement est déjà enregistré.");
        }
        long dejaConfirmes = recrutements.countBySelection_IdOffreB2AndEtatRecrutement_Code(
                selection.getIdOffreB2(), "CONFIRME")
                + recrutements.countBySelection_IdOffreB2AndEtatRecrutement_Code(
                selection.getIdOffreB2(), "TRANSMIS_BLOC4");
        Integer limiteExistante = recrutements
                .findFirstBySelection_IdOffreB2OrderByIdRecrutementAsc(selection.getIdOffreB2())
                .map(RecrutementModel::getNombrePostes).orElse(null);
        if (limiteExistante != null && !limiteExistante.equals(nombrePostes)) {
            throw new Bloc3Exception("Le nombre de postes doit rester identique pour tous les candidats de cette offre.");
        }
        if (dejaConfirmes >= nombrePostes) {
            throw new Bloc3Exception("Tous les postes autorisés pour cette offre sont déjà pourvus.");
        }

        RecrutementModel recrutement = new RecrutementModel();
        recrutement.setSelection(selection);
        recrutement.setNombrePostes(nombrePostes);
        recrutement.setEtatRecrutement(etatRecrutement("CONFIRME"));
        recrutement.setConfirmePar(auteur);
        recrutement.setConfirmeLe(LocalDate.now());
        recrutement.setEtatTransfert(etatTransfert("A_TRANSMETTRE"));
        RecrutementModel cree = recrutements.save(recrutement);
        historiser("RECRUTEMENT", cree.getIdRecrutement(), "RECRUTEMENT_CONFIRME", auteur,
                "Recrutement confirmé. Préparer le transfert vers le Bloc 4.");
    }

    @Transactional
    public void enregistrerTransfert(Integer idSelection, boolean transmis, String referenceBloc4,
                                      String erreur, Integer auteur) {
        RecrutementModel recrutement = recrutements.findBySelection_IdSelection(idSelection)
                .orElseThrow(() -> new Bloc3Exception("Confirmez le recrutement avant le transfert."));
        if ("TRANSMIS".equals(recrutement.getEtatTransfert().getCode())) {
            throw new Bloc3Exception("Le transfert a déjà été confirmé par le Bloc 4.");
        }
        if (transmis && (referenceBloc4 == null || referenceBloc4.isBlank())) {
            throw new Bloc3Exception("La référence de réception du Bloc 4 est obligatoire.");
        }
        if (!transmis && (erreur == null || erreur.isBlank())) {
            throw new Bloc3Exception("Indiquez le motif de l'échec du transfert.");
        }
        recrutement.setEtatTransfert(etatTransfert(transmis ? "TRANSMIS" : "ECHEC"));
        recrutement.setTransfertLe(LocalDate.now());
        recrutement.setReferenceBloc4(transmis ? referenceBloc4.trim() : null);
        recrutement.setErreurTransfert(transmis ? null : erreur.trim());
        if (transmis) {
            recrutement.setEtatRecrutement(etatRecrutement("TRANSMIS_BLOC4"));
        }
        RecrutementModel sauvegarde = recrutements.save(recrutement);
        historiser("RECRUTEMENT", sauvegarde.getIdRecrutement(),
                transmis ? "TRANSFERT_REUSSI" : "TRANSFERT_ECHEC", auteur,
                transmis ? "Référence Bloc 4 : " + referenceBloc4.trim() : erreur.trim());
    }

    private void verifierDecisionValidee(SelectionModel selection) {
        if (selection.getDecision() == null || !"RETENU".equals(selection.getDecision().getCode())) {
            throw new Bloc3Exception("La candidature doit être retenue avant de poursuivre.");
        }
        if (selection.isValidationObligatoire() && !"VALIDEE".equals(selection.getEtatValidation().getCode())) {
            throw new Bloc3Exception("La décision doit être validée avant la proposition d'embauche.");
        }
    }

    private EtatValidationModel etatValidation(String code) {
        return reference(etatsValidation.findByCode(code).orElse(null), "état de validation");
    }

    private EtatPropositionModel etatProposition(String code) {
        return reference(etatsProposition.findByCode(code).orElse(null), "état de proposition");
    }

    private EtatRecrutementModel etatRecrutement(String code) {
        return reference(etatsRecrutement.findByCode(code).orElse(null), "état de recrutement");
    }

    private EtatTransfertModel etatTransfert(String code) {
        return reference(etatsTransfert.findByCode(code).orElse(null), "état de transfert");
    }

    private static <T> T reference(T valeur, String nom) {
        if (valeur == null) {
            throw new Bloc3Exception("La référence « " + nom + " » est absente. Exécutez le script SQL du Bloc 3.");
        }
        return valeur;
    }

    private void historiser(String type, Integer id, String action, Integer auteur, String commentaire) {
        HistoriqueModel ligne = new HistoriqueModel();
        ligne.setTypeElement(type);
        ligne.setIdElement(id);
        ligne.setAction(action);
        ligne.setAuteurId(auteur);
        ligne.setCommentaire(commentaire);
        ligne.setDateAction(LocalDateTime.now());
        historiques.save(ligne);
    }

    private static String texte(String valeur) {
        return valeur == null || valeur.isBlank() ? null : valeur.trim();
    }

    private static void verifierScore(BigDecimal score, int maximum, String nom) {
        if (score != null && (score.signum() < 0 || score.compareTo(BigDecimal.valueOf(maximum)) > 0)) {
            throw new Bloc3Exception("La note de " + nom + " doit être comprise entre 0 et " + maximum + ".");
        }
    }
}
