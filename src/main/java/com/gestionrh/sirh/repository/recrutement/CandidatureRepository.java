package com.gestionrh.sirh.recrutement.repository;

import com.gestionrh.sirh.recrutement.enums.CandidatureStatut;
import com.gestionrh.sirh.recrutement.enums.Classement;
import com.gestionrh.sirh.recrutement.model.Candidature;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

    Optional<Candidature> findByNumero(String numero);

    Page<Candidature> findByOffre_IdOffre(Integer idOffre, Pageable pageable);

    Page<Candidature> findByOffre_IdOffreAndClassementAuto(Integer idOffre, Classement classement, Pageable pageable);

    Page<Candidature> findByStatut(CandidatureStatut statut, Pageable pageable);

    /** Reserve de candidatures spontanees non rattachees. */
    Page<Candidature> findByOffreIsNullAndSpontaneeTrue(Pageable pageable);

    /** Detection des doubles candidatures (statut a exclure : RETIREE). */
    boolean existsByCandidat_IdCandidatAndOffre_IdOffreAndStatutNot(Integer idCandidat, Integer idOffre, CandidatureStatut statutExclu);

    /** Pour mettre a jour offre.nbCandidatures. */
    long countByOffre_IdOffreAndStatutNot(Integer idOffre, CandidatureStatut statutExclu);

    /** Rappel au RH : dossiers en attente (statuts RECUE / A_ETUDIER) recus avant la date limite de traitement. */
    List<Candidature> findByStatutInAndDateReceptionBefore(Collection<CandidatureStatut> statuts, LocalDateTime avant);
}
