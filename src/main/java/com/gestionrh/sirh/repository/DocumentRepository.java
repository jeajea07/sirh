package com.gestionrh.sirh.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.DocumentModel;

public interface DocumentRepository extends JpaRepository<DocumentModel, Integer> {

    @EntityGraph(attributePaths = {"employe", "contrat", "onboardingTache"})
    @Query("select d from DocumentModel d where d.employe.id = :id order by d.dateDepot desc")
    List<DocumentModel> findByIdEmployeOrderByDateDepotDesc(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"employe", "contrat", "onboardingTache"})
    @Query("select d from DocumentModel d where d.contrat.id = :id order by d.version desc")
    List<DocumentModel> findByIdContratOrderByVersionDesc(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"employe", "contrat", "onboardingTache"})
    @Query("select d from DocumentModel d where d.onboardingTache.id = :id order by d.dateDepot desc")
    List<DocumentModel> findByIdOnboardingTacheOrderByDateDepotDesc(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"employe", "contrat", "onboardingTache"})
    Page<DocumentModel> findByTypeDocument(String type, Pageable pageable);

    @EntityGraph(attributePaths = {"employe", "contrat", "onboardingTache"})
    Page<DocumentModel> findByStatutValidation(String statut, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"employe", "contrat", "onboardingTache"})
    Page<DocumentModel> findAll(Pageable pageable);

    long countByStatutValidationIn(Collection<String> statuts);
}
