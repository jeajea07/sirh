package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.ContratModel;

public interface ContratRepository extends JpaRepository<ContratModel, Integer> {

    @Query("select c from ContratModel c where c.employe.id = :id order by c.dateDebut desc")
    List<ContratModel> findByIdEmployeOrderByDateDebutDesc(@Param("id") Integer id);

    @Query("select c from ContratModel c where c.employe.id = :id and c.statutWorkflow in :statuts order by c.dateDebut desc")
    List<ContratModel> findByIdEmployeAndStatutWorkflowInOrderByDateDebutDesc(
            @Param("id") Integer id, @Param("statuts") Collection<String> statuts);

    Page<ContratModel> findByStatutWorkflow(String statut, Pageable pageable);

    Page<ContratModel> findByTypeOperation(String typeOperation, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"employe", "typeContrat"})
    Page<ContratModel> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"employe", "typeContrat"})
    @Query("select c from ContratModel c where lower(c.employe.nom) like lower(concat('%', :recherche, '%')) "
            + "or lower(c.employe.prenom) like lower(concat('%', :recherche, '%')) "
            + "or lower(c.reference) like lower(concat('%', :recherche, '%'))")
    Page<ContratModel> rechercher(@Param("recherche") String recherche, Pageable pageable);

    Optional<ContratModel> findByIdRecrutementExterne(Integer idRecrutement);

    List<ContratModel> findByContratPrecedent_IdOrderByVersionDesc(Integer idContratPrecedent);

    @EntityGraph(attributePaths = {"employe", "typeContrat", "contratPrecedent", "creePar"})
    @Query("select c from ContratModel c where c.id = :id")
    Optional<ContratModel> findAvecRelationsById(@Param("id") Integer id);

    Optional<ContratModel> findFirstByOrderByIdDesc();

    Optional<ContratModel> findFirstByReferenceStartingWithOrderByReferenceDesc(String prefix);

    @Query("select c from ContratModel c where c.dateFin between :debut and :fin and c.statutWorkflow = 'EN_VIGUEUR' order by c.dateFin asc")
    List<ContratModel> echeances(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    long countByStatutWorkflow(String statut);

    long countByStatutWorkflowIn(Collection<String> statuts);

    @Query("select c from ContratModel c where c.statutWorkflow = 'EN_VIGUEUR' and c.dateDebut between :debut and :fin")
    List<ContratModel> arriveesPrevues(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("select count(c) > 0 from ContratModel c where c.employe.id = :id and c.statutWorkflow in :statuts")
    boolean existsByIdEmployeAndStatutWorkflowIn(@Param("id") Integer id, @Param("statuts") Collection<String> statuts);
}
