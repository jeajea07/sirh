package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.ModeleOnboardingModel;

public interface ModeleOnboardingRepository extends JpaRepository<ModeleOnboardingModel, Integer> {

    @EntityGraph(attributePaths = {"taches"})
    List<ModeleOnboardingModel> findByActifTrueOrderByNomAscVersionDesc();

    @EntityGraph(attributePaths = {"taches"})
    List<ModeleOnboardingModel> findAllByOrderByNomAscVersionDesc();

    @EntityGraph(attributePaths = {"taches"})
    @org.springframework.data.jpa.repository.Query("select m from ModeleOnboardingModel m where m.id = :id")
    java.util.Optional<ModeleOnboardingModel> findAvecTachesById(@Param("id") Integer id);

    Optional<ModeleOnboardingModel> findFirstByNomOrderByVersionDesc(String nom);

    List<ModeleOnboardingModel> findByNomOrderByVersionDesc(String nom);

    boolean existsByNomAndVersion(String nom, Integer version);

    @Query("select m from ModeleOnboardingModel m where m.actif = true "
            + "and (m.poste.id = :idPoste or m.poste is null) "
            + "and (m.departement.id = :idDepartement or m.departement is null)")
    List<ModeleOnboardingModel> candidatsPour(@Param("idPoste") Integer idPoste,
                                               @Param("idDepartement") Integer idDepartement);
}
