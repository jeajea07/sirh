package com.gestionrh.sirh.service.recrutement;

import com.gestionrh.sirh.dto.recrutement.CandidatureDetail;
import com.gestionrh.sirh.dto.recrutement.CandidatureLigne;
import com.gestionrh.sirh.dto.recrutement.IndicateursCandidature;
import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.enums.recrutement.TypeCritere;
import com.gestionrh.sirh.exception.recrutement.RessourceIntrouvableException;
import com.gestionrh.sirh.model.recrutement.Candidature;
import com.gestionrh.sirh.model.recrutement.HistoriqueCandidature;
import com.gestionrh.sirh.repository.recrutement.CandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.HistoriqueCandidatureRepository;
import com.gestionrh.sirh.repository.recrutement.OffreRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SuiviCandidatureService {

    private final CandidatureRepository candidatures;
    private final OffreRepository offres;
    private final HistoriqueCandidatureRepository historiques;
    private final ClassementCandidatureService classeur;
    private final JoursOuvrablesService joursOuvrables;

    public SuiviCandidatureService(CandidatureRepository candidatures, OffreRepository offres,
            HistoriqueCandidatureRepository historiques, ClassementCandidatureService classeur,
            JoursOuvrablesService joursOuvrables) {
        this.candidatures = candidatures;
        this.offres = offres;
        this.historiques = historiques;
        this.classeur = classeur;
        this.joursOuvrables = joursOuvrables;
    }

    public Page<CandidatureLigne> rechercher(Integer offreId, Classement classement,
            com.gestionrh.sirh.enums.recrutement.CandidatureStatut statut, String q, Pageable pageable) {
        return candidatures.rechercher(offreId, classement, statut, q == null ? "" : q.trim(), pageable)
                .map(c -> new CandidatureLigne(c.getIdCandidature(), c.getNumero(),
                        c.getCandidat().getPrenom() + " " + c.getCandidat().getNom(), c.getCandidat().getEmail(),
                        c.getOffre() == null ? "Candidature spontanée" : c.getOffre().getTitre(),
                        c.getClassementAuto(), c.getStatut(), c.getDateReception()));
    }

    public List<com.gestionrh.sirh.model.recrutement.Offre> toutesLesOffres() { return offres.findAll(); }

    public IndicateursCandidature indicateurs() {
        var enEtude = candidatures.findByStatut(CandidatureStatut.A_ETUDIER);
        var aujourdhui = java.time.LocalDate.now(java.time.ZoneId.of("Indian/Antananarivo"));
        return new IndicateursCandidature(
                enEtude.stream().filter(c -> c.getClassementAuto() == Classement.CONFORME).count(),
                enEtude.stream().filter(c -> c.getClassementAuto() == Classement.NON_CONFORME).count(),
                enEtude.stream().filter(c -> c.getClassementAuto() == Classement.A_REGARDER).count(),
                enEtude.stream().filter(c -> joursOuvrables.estEnRetard(c.getDateReception(), aujourdhui)).count());
    }

    public CandidatureDetail detail(Integer id) {
        Candidature c = candidatures.findWithCandidatAndOffreByIdCandidature(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Candidature introuvable."));
        List<String> competences = c.getCompetences().stream().map(v -> v.getLibelle()).toList();
        List<com.gestionrh.sirh.model.recrutement.CritereOffre> criteres = c.getOffre() == null ? List.of()
                : c.getOffre().getCriteres();
        ResultatClassement analyse = c.getOffre() == null ? null : classeur.classer(c, criteres, competences);
        return new CandidatureDetail(c.getIdCandidature(), c.getNumero(), c.getCandidat().getNom(),
                c.getCandidat().getPrenom(), c.getCandidat().getEmail(), c.getCandidat().getTelephone(),
                c.getCandidat().getAdresse(), c.getOffre() == null ? "Candidature spontanée" : c.getOffre().getTitre(),
                c.getStatut(), c.getClassementAuto(), c.getClassementFinal(), c.getMotifClassement(),
                c.getNiveauEtude(), c.getAnneesExperience(), c.getFormations(), c.getExperiences(), c.getLangues(),
                c.getDisponibilite(), c.getCvNomFichier(), competences, c.getDateReception(),
                analyse == null ? List.of() : analyse.obligatoires(), analyse == null ? List.of() : analyse.souhaites());
    }

    public Candidature chargerAvecCandidat(Integer id) {
        return candidatures.findWithCandidatAndOffreByIdCandidature(id)
                .orElseThrow(() -> new RessourceIntrouvableException("Candidature introuvable."));
    }

    public List<HistoriqueCandidature> historique(Integer id) {
        return historiques.findByCandidature_IdCandidatureOrderByDateActionDesc(id);
    }
}
