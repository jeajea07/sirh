package com.gestionrh.sirh.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.OnboardingModel;

public interface OnboardingRepository extends JpaRepository<OnboardingModel, Integer> {

    @EntityGraph(attributePaths = {"employe", "contrat", "modele"})
    Page<OnboardingModel> findByStatut(String statut, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"employe", "contrat", "modele"})
    Page<OnboardingModel> findAll(Pageable pageable);

    @Query("select o from OnboardingModel o where o.employe.id = :id order by o.dateCreation desc")
    List<OnboardingModel> findByIdEmployeOrderByDateCreationDesc(@Param("id") Integer id);

    @Query("select o from OnboardingModel o where o.contrat.id = :idContrat order by o.dateCreation desc")
    java.util.List<OnboardingModel> findByContrat_IdOrderByDateCreationDesc(@Param("idContrat") Integer idContrat);

    java.util.Optional<OnboardingModel> findFirstByContrat_IdOrderByDateCreationDesc(Integer idContrat);

    @EntityGraph(attributePaths = {"employe", "contrat", "modele", "responsableRh", "cloturePar"})
    @Query("select o from OnboardingModel o where o.id = :id")
    Optional<OnboardingModel> findAvecRelationsById(@Param("id") Integer id);

    Optional<OnboardingModel> findFirstByReferenceStartingWithOrderByReferenceDesc(String prefix);

    @Query("select count(o) > 0 from OnboardingModel o where o.modele.id = :idModele")
    boolean existsByIdModeleOnboarding(@Param("idModele") Integer idModele);

    long countByStatutIn(Collection<String> statuts);

    @Query("select o from OnboardingModel o where o.statut = 'A_VERIFIER' order by o.dateFinPrevue asc")
    List<OnboardingModel> aCloturer();

    Optional<OnboardingModel> findFirstByOrderByIdDesc();
}
