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

    @EntityGraph(attributePaths = {"employe", "contrat", "onboardingTache", "typeAdministratif"})
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

    /** Bloc 5 — pièces vérifiées dont la validité doit être surveillée chaque jour (RG-DO05). */
    @EntityGraph(attributePaths = {"employe", "typeAdministratif"})
    @Query("select d from DocumentModel d where d.dateExpiration is not null "
            + "and d.statutValidation in ('VERIFIE', 'A_RENOUVELER')")
    List<DocumentModel> documentsASurveiller();

    /** Bloc 5 — versions encore actives d'une pièce (hors versions remplacées), de la plus récente à la plus ancienne. */
    @Query("select d from DocumentModel d where d.employe.id = :idEmploye "
            + "and d.typeAdministratif.id = :idType and d.statutValidation <> 'REMPLACE' order by d.dateDepot desc")
    List<DocumentModel> versionsActives(@Param("idEmploye") Integer idEmploye, @Param("idType") Integer idType);
}
