package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.dto.recrutement.CandidatureForm;
import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.event.recrutement.CandidatureRecueEvent;
import com.gestionrh.sirh.exception.recrutement.RegleMetierException;
import com.gestionrh.sirh.model.recrutement.Candidat;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.model.recrutement.CritereOffre;
import com.gestionrh.sirh.model.recrutement.CvCompetence;
import com.gestionrh.sirh.model.recrutement.HistoriqueCandidature;
import com.gestionrh.sirh.model.recrutement.Offre;
import com.gestionrh.sirh.repository.recrutement.CandidatRepository;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.CritereOffreRepository;
import com.gestionrh.sirh.repository.recrutement.HistoriqueCandidatureRepository;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidatureService {

    private final CandidatRepository candidats;
    private final CandidatureRepository candidatures;
    private final CritereOffreRepository criteres;
    private final HistoriqueCandidatureRepository historiques;
    private final OffreService offreService;
    private final StockageCvService stockageCv;
    private final ClassementCandidatureService classementService;
    private final ApplicationEventPublisher events;

    public CandidatureService(CandidatRepository candidats, CandidatureRepository candidatures,
            CritereOffreRepository criteres, HistoriqueCandidatureRepository historiques,
            OffreService offreService, StockageCvService stockageCv,
            ClassementCandidatureService classementService, ApplicationEventPublisher events) {
        this.candidats = candidats;
        this.candidatures = candidatures;
        this.criteres = criteres;
        this.historiques = historiques;
        this.offreService = offreService;
        this.stockageCv = stockageCv;
        this.classementService = classementService;
        this.events = events;
    }

    @Transactional
    public Candidature recevoir(Integer idOffre, CandidatureForm form) {
        Offre offre = idOffre == null ? null : offreService.trouverOuverte(idOffre);
        CvStocke cv = stockageCv.stocker(form.getFichierCv());
        try {
            String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
            candidats.verrouillerEmailPourCreation(email);
            Candidat candidat = candidats.findByEmailIgnoreCase(email).orElseGet(Candidat::new);
            candidat.setNom(form.getNom().trim());
            candidat.setPrenom(form.getPrenom().trim());
            candidat.setEmail(email);
            candidat.setTelephone(form.getTelephone());
            candidat.setAdresse(form.getAdresse());
            candidat = candidats.saveAndFlush(candidat);

            if (offre != null && candidatures.existsByCandidat_IdCandidatAndOffre_IdOffreAndStatutNot(
                    candidat.getIdCandidat(), offre.getIdOffre(), CandidatureStatut.RETIREE)) {
                throw new RegleMetierException("Vous avez déjà déposé une candidature pour cette offre.");
            }

            Candidature candidature = new Candidature();
            candidature.setNumero("TMP-" + UUID.randomUUID());
            candidature.setCandidat(candidat);
            candidature.setOffre(offre);
            candidature.setSpontanee(offre == null);
            candidature.setDateReception(LocalDateTime.now());
            candidature.setNiveauEtude(form.getNiveauEtude());
            candidature.setAnneesExperience(form.getAnneesExperience());
            candidature.setFormations(form.getFormations());
            candidature.setExperiences(form.getExperiences());
            candidature.setLangues(form.getLangues());
            candidature.setDisponibilite(form.getDisponibilite());
            candidature.setCvNomFichier(limite(form.getFichierCv().getOriginalFilename(), 255));
            candidature.setCvChemin(cv.chemin());
            candidature.setCvType(cv.type());
            candidature.setCvTailleOctets(cv.tailleOctets());
            candidature.setStatut(CandidatureStatut.RECUE);
            for (String libelle : form.getCompetences()) {
                if (libelle != null && !libelle.isBlank()) {
                    CvCompetence competence = new CvCompetence();
                    competence.setLibelle(limite(libelle.trim(), 100));
                    candidature.addCompetence(competence);
                }
            }
            candidature = candidatures.saveAndFlush(candidature);
            candidature.setNumero("CAND-%d-%05d".formatted(Year.now().getValue(), candidature.getIdCandidature()));
            historiser(candidature, "RECEPTION", null, CandidatureStatut.RECUE, null, null, "Dossier reçu");

            if (offre != null) {
                List<String> competences = candidature.getCompetences().stream().map(CvCompetence::getLibelle).toList();
                List<CritereOffre> criteresOffre = criteres.findByOffre_IdOffre(offre.getIdOffre());
                ResultatClassement resultat = classementService.classer(candidature, criteresOffre, competences);
                candidature.setClassementAuto(resultat.classement());
                candidature.setMotifClassement(resultat.motif());
                candidature.setStatut(CandidatureStatut.A_ETUDIER);
                candidatures.save(candidature);
                historiser(candidature, "CLASSEMENT_AUTO", CandidatureStatut.RECUE, CandidatureStatut.A_ETUDIER,
                        null, resultat.classement(), resultat.motif());
                offreService.recalculerNombreCandidatures(offre.getIdOffre());
            }

            events.publishEvent(new CandidatureRecueEvent(candidature.getIdCandidature()));
            return candidature;
        } catch (DataIntegrityViolationException e) {
            supprimerCvSansMasquerErreur(cv.chemin());
            String detail = e.getMostSpecificCause() == null ? "" : String.valueOf(e.getMostSpecificCause().getMessage());
            if (detail.contains("uq_candidature_candidat_offre")) {
                throw new RegleMetierException("Vous avez déjà déposé une candidature pour cette offre.");
            }
            throw e;
        } catch (RuntimeException e) {
            supprimerCvSansMasquerErreur(cv.chemin());
            throw e;
        }
    }

    private void historiser(Candidature candidature, String action, CandidatureStatut ancienStatut,
            CandidatureStatut nouveauStatut, Classement ancienClassement, Classement nouveauClassement, String commentaire) {
        HistoriqueCandidature h = new HistoriqueCandidature();
        h.setCandidature(candidature);
        h.setAction(action);
        h.setAncienStatut(ancienStatut);
        h.setNouveauStatut(nouveauStatut);
        h.setAncienClassement(ancienClassement);
        h.setNouveauClassement(nouveauClassement);
        h.setCommentaire(commentaire);
        historiques.save(h);
    }

    private void supprimerCvSansMasquerErreur(String chemin) {
        try { stockageCv.supprimer(chemin); } catch (RuntimeException ignored) { }
    }

    private String limite(String texte, int longueur) {
        if (texte == null || texte.length() <= longueur) return texte;
        return texte.substring(0, longueur);
    }
}
