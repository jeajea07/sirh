package com.gestionrh.sirh.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.PlanningModel;

public interface PlanningRepository extends JpaRepository<PlanningModel, Integer> {

    List<PlanningModel> findByStatutInOrderByDateDebutAsc(Collection<String> statuts);

    List<PlanningModel> findByDepartement_IdInOrderByDateDebutDesc(Collection<Integer> departements);

    List<PlanningModel> findAllByOrderByDateDebutDesc();

    /** Plannings encore « vivants » qui chevauchent une période (pour éviter deux plannings concurrents). */
    @Query("select p from PlanningModel p where p.departement.id = :dep and p.dateDebut <= :fin and p.dateFin >= :debut "
            + "and p.statut not in ('MODIFIE', 'REJETE')")
    List<PlanningModel> chevauchants(@Param("dep") Integer idDepartement, @Param("debut") LocalDate debut,
                                     @Param("fin") LocalDate fin);

    /** Coût des heures majorées déjà engagé sur l'année par les plannings validés du département. */
    @Query("select coalesce(sum(p.coutEstime), 0) from PlanningModel p where p.departement.id = :dep "
            + "and p.statut in ('PUBLIE', 'CLOTURE') and year(p.dateDebut) = :annee and p.id <> :exclu")
    BigDecimal coutEngage(@Param("dep") Integer idDepartement, @Param("annee") int annee, @Param("exclu") Integer exclu);

    @Query("select count(p) from PlanningModel p where p.reference like concat(:prefixe, '%')")
    long compterReferences(@Param("prefixe") String prefixe);
}
