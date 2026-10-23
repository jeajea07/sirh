package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.AffectationModel;

public interface AffectationRepository extends JpaRepository<AffectationModel, Integer> {

    @EntityGraph(attributePaths = {"employe", "poste", "departement", "responsable", "contrat"})
    @Query("select a from AffectationModel a where a.employe.id = :id order by a.dateDebut desc")
    List<AffectationModel> findByIdEmployeOrderByDateDebutDesc(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"employe", "poste", "departement", "responsable", "contrat"})
    @Query("select a from AffectationModel a where a.employe.id = :id and a.statut = :statut order by a.dateDebut desc")
    List<AffectationModel> findParEmployeEtStatut(@Param("id") Integer id, @Param("statut") String statut);

    @EntityGraph(attributePaths = {"employe", "poste", "departement", "responsable", "contrat"})
    @Query("select a from AffectationModel a where a.employe.id = :id and a.statut = :statut order by a.dateDebut desc")
    Optional<AffectationModel> findFirstByIdEmployeAndStatutOrderByDateDebutDesc(
            @Param("id") Integer id, @Param("statut") String statut);

    @Query("select count(a) from AffectationModel a where a.employe.id = :idEmploye "
            + "and (:idExclu is null or a.id <> :idExclu) "
            + "and a.statut = 'ACTIVE' and a.dateDebut <= :fin and (a.dateFin is null or a.dateFin >= :debut)")
    long countChevauchements(@Param("idEmploye") Integer idEmploye,
                             @Param("idExclu") Integer idExclu,
                             @Param("debut") LocalDate debut,
                             @Param("fin") LocalDate fin);

    @EntityGraph(attributePaths = {"employe", "poste", "departement", "responsable"})
    List<AffectationModel> findByStatut(String statut);

    @Override
    @EntityGraph(attributePaths = {"employe", "poste", "departement", "responsable", "contrat"})
    List<AffectationModel> findAll();
}
