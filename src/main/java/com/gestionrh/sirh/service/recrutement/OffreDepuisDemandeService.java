package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.dto.recrutement.DemandeOffreResume;
import com.gestionrh.sirh.dto.recrutement.OffreForm;
import com.gestionrh.sirh.enums.recrutement.NatureCritere;
import com.gestionrh.sirh.enums.recrutement.NiveauEtude;
import com.gestionrh.sirh.enums.recrutement.TypeCritere;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.event.recrutement.OffreCreeeEvent;
import com.gestionrh.sirh.model.CritereRecrutementModel;
import com.gestionrh.sirh.model.DemandeBesoinModel;
import com.gestionrh.sirh.model.recrutement.CritereOffre;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.repository.DemandeBesoinRepository;
import com.gestionrh.sirh.repository.recrutement.OffreRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Adaptateur BLOC 2A vers le bloc 1, sans modification de ses entités ni de son workflow. */
@Service
@Transactional(readOnly = true)
public class OffreDepuisDemandeService {

    private final DemandeBesoinRepository demandes;
    private final OffreRepository offres;
    private final OffreService offreService;
    private final ApplicationEventPublisher events;

    public OffreDepuisDemandeService(DemandeBesoinRepository demandes, OffreRepository offres, OffreService offreService,
            ApplicationEventPublisher events) {
        this.demandes = demandes;
        this.offres = offres;
        this.offreService = offreService;
        this.events = events;
    }

    public List<DemandeOffreResume> demandesAccepteesSansOffre() {
        List<DemandeOffreResume> resultat = new ArrayList<>();
        for (DemandeBesoinModel demande : demandes.findAll()) {
            if (!demandeAcceptee(demande) || offres.existsByIdDemandeBesoinAndStatutNotIn(
                    demande.getId(), List.of(com.gestionrh.sirh.enums.recrutement.OffreStatut.ANNULEE,
                            com.gestionrh.sirh.enums.recrutement.OffreStatut.TERMINEE))) continue;
            resultat.add(new DemandeOffreResume(demande.getId(), demande.getNumero(),
                    demande.getPoste() == null ? "Poste non renseigné" : demande.getPoste().getDesignation(),
                    demande.getDepartement() == null ? "Département non renseigné" : demande.getDepartement().getDesignation()));
        }
        return resultat;
    }

    @Transactional
    public Offre creerDepuisDemande(Integer idDemande) {
        DemandeBesoinModel demande = demandes.findById(idDemande)
                .orElseThrow(() -> new RegleMetierException("Demande de besoin introuvable."));
        if (!demandeAcceptee(demande)) throw new RegleMetierException("La demande n'a pas terminé avec succès son circuit de validation.");
        List<com.gestionrh.sirh.enums.recrutement.OffreStatut> statutsFermes = List.of(
                com.gestionrh.sirh.enums.recrutement.OffreStatut.ANNULEE,
                com.gestionrh.sirh.enums.recrutement.OffreStatut.TERMINEE);
        Offre existante = offres.findByIdDemandeBesoinAndStatutNotIn(idDemande, statutsFermes).orElse(null);
        if (existante != null) return existante;

        OffreForm form = new OffreForm();
        form.setIdDemandeBesoin(idDemande);
        form.setIdPoste(demande.getPoste().getId());
        form.setIdDepartement(demande.getDepartement().getId());
        form.setIdTypeContrat(demande.getTypeContrat().getId());
        form.setTitre(demande.getPoste().getDesignation());
        form.setDescription(demande.getJustification());
        form.setNombrePostes(demande.getNombrePostes());
        form.setDureeMois(demande.getDureeMois());
        form.setSalairePropose(demande.getSalairePropose());
        String missions = demande.getCritereRecrutement() == null ? null : demande.getCritereRecrutement().getMissions();
        // Les missions sont facultatives dans la demande mais obligatoires pour publier : repli sur la justification.
        form.setMissions(missions == null || missions.isBlank() ? demande.getJustification() : missions);
        form.setDateLimite(null); // Le RH choisit la date avant validation.

        Offre offre = offreService.creer(form);
        copierCriteres(demande.getCritereRecrutement(), offre);
        events.publishEvent(new OffreCreeeEvent(offre.getIdOffre()));
        return offre;
    }

    private boolean demandeAcceptee(DemandeBesoinModel demande) {
        // Le bloc 1 pose statut = VALIDEE uniquement lors de la décision finale de la DG.
        // L'étape RESPONSABLE déclarée dans circuit_validation n'est pas parcourue par le workflow
        // (RH → FINANCE → DG) : on ne peut donc pas exiger une validation sur chaque étape du circuit.
        return "VALIDEE".equals(demande.getStatut());
    }

    private void copierCriteres(CritereRecrutementModel source, Offre offre) {
        if (source == null) return;
        if (source.getAnneeExperience() != null) ajouterCritere(offre, NatureCritere.EXPERIENCE, null, source.getAnneeExperience());
        if (source.getNiveauEtude() != null) {
            try {
                NiveauEtude niveau = NiveauEtude.valueOf(source.getNiveauEtude().trim().toUpperCase());
                ajouterCritere(offre, NatureCritere.NIVEAU_ETUDE, niveau.name(), null);
            } catch (IllegalArgumentException e) {
                // Une valeur hors enum B2A reste à compléter par le RH dans le brouillon.
            }
        }
        for (String competence : decouperListe(source.getCompetence())) {
            ajouterCritere(offre, NatureCritere.COMPETENCE, competence, null);
        }
    }

    private void ajouterCritere(Offre offre, NatureCritere nature, String libelle, Integer minimum) {
        CritereOffre critere = new CritereOffre();
        critere.setOffre(offre);
        critere.setTypeCritere(TypeCritere.OBLIGATOIRE);
        critere.setNature(nature);
        critere.setLibelle(libelle);
        critere.setValeurMin(minimum);
        offre.getCriteres().add(critere);
    }

    private List<String> decouperListe(String texte) {
        if (texte == null || texte.isBlank()) return List.of();
        return java.util.Arrays.stream(texte.split("[,;\\n]"))
                .map(String::trim).filter(s -> !s.isEmpty()).distinct().toList();
    }

}
