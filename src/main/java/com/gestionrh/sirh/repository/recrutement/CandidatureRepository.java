package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.enums.recrutement.CandidatureStatut;
import com.gestionrh.sirh.enums.recrutement.Classement;
import com.gestionrh.sirh.model.recrutement.Candidature;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatureRepository extends JpaRepository<Candidature, Integer> {

    Optional<Candidature> findByNumero(String numero);

    @EntityGraph(attributePaths = {"candidat", "offre"})
    Optional<Candidature> findWithCandidatAndOffreByIdCandidature(Integer idCandidature);

    boolean existsByOffre_IdOffre(Integer idOffre);

    @EntityGraph(attributePaths = "candidat")
    List<Candidature> findByOffre_IdOffreAndStatutIn(Integer idOffre, Collection<CandidatureStatut> statuts);

    Page<Candidature> findByOffre_IdOffre(Integer idOffre, Pageable pageable);

    Page<Candidature> findByOffre_IdOffreAndClassementAuto(Integer idOffre, Classement classement, Pageable pageable);

    Page<Candidature> findByStatut(CandidatureStatut statut, Pageable pageable);

    /** Reserve de candidatures spontanees non rattachees. */
    Page<Candidature> findByOffreIsNullAndSpontaneeTrue(Pageable pageable);

    @EntityGraph(attributePaths = "candidat")
    @Query(value = "select c from Candidature c where c.offre is null and c.spontanee = true "
            + "and (:q = '' or lower(c.numero) like lower(concat('%', :q, '%')) "
            + "or lower(c.candidat.nom) like lower(concat('%', :q, '%')) "
            + "or lower(c.candidat.prenom) like lower(concat('%', :q, '%')) "
            + "or lower(c.candidat.email) like lower(concat('%', :q, '%'))) ",
            countQuery = "select count(c) from Candidature c where c.offre is null and c.spontanee = true "
                    + "and (:q = '' or lower(c.numero) like lower(concat('%', :q, '%')) "
                    + "or lower(c.candidat.nom) like lower(concat('%', :q, '%')) "
                    + "or lower(c.candidat.prenom) like lower(concat('%', :q, '%')) "
                    + "or lower(c.candidat.email) like lower(concat('%', :q, '%'))) ")
    Page<Candidature> rechercherReserve(@Param("q") String q, Pageable pageable);

    @EntityGraph(attributePaths = {"candidat", "offre"})
    @Query(value = "select c from Candidature c where (:idOffre is null or c.offre.idOffre = :idOffre) "
            + "and (:classement is null or c.classementAuto = :classement) "
            + "and (:statut is null or c.statut = :statut) "
            + "and (:q = '' or lower(c.numero) like lower(concat('%', :q, '%')) "
            + "or lower(c.candidat.nom) like lower(concat('%', :q, '%')) "
            + "or lower(c.candidat.prenom) like lower(concat('%', :q, '%')) "
            + "or lower(c.candidat.email) like lower(concat('%', :q, '%'))) ",
            countQuery = "select count(c) from Candidature c where (:idOffre is null or c.offre.idOffre = :idOffre) "
                    + "and (:classement is null or c.classementAuto = :classement) "
                    + "and (:statut is null or c.statut = :statut) "
                    + "and (:q = '' or lower(c.numero) like lower(concat('%', :q, '%')) "
                    + "or lower(c.candidat.nom) like lower(concat('%', :q, '%')) "
                    + "or lower(c.candidat.prenom) like lower(concat('%', :q, '%')) "
                    + "or lower(c.candidat.email) like lower(concat('%', :q, '%'))) ")
    Page<Candidature> rechercher(@Param("idOffre") Integer idOffre, @Param("classement") Classement classement,
            @Param("statut") CandidatureStatut statut, @Param("q") String q, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Candidature c where c.idCandidature = :id")
    Optional<Candidature> verrouillerParId(@Param("id") Integer id);

    /** Detection des doubles candidatures (statut a exclure : RETIREE). */
    boolean existsByCandidat_IdCandidatAndOffre_IdOffreAndStatutNot(Integer idCandidat, Integer idOffre, CandidatureStatut statutExclu);

    /** Pour mettre a jour offre.nbCandidatures. */
    long countByOffre_IdOffreAndStatutNot(Integer idOffre, CandidatureStatut statutExclu);

    /** Rappel au RH : dossiers en attente (statuts RECUE / A_ETUDIER) recus avant la date limite de traitement. */
    List<Candidature> findByStatutInAndDateReceptionBefore(Collection<CandidatureStatut> statuts, LocalDateTime avant);

    @EntityGraph(attributePaths = {"candidat", "offre"})
    List<Candidature> findByStatut(CandidatureStatut statut);

    long countByStatutAndClassementAuto(CandidatureStatut statut, Classement classement);
}
