package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.PlanningLigneModel;

public interface PlanningLigneRepository extends JpaRepository<PlanningLigneModel, Integer> {

    @Query("select l from PlanningLigneModel l where l.planning.id = :id order by l.employe.nom, l.jour")
    List<PlanningLigneModel> duPlanning(@Param("id") Integer idPlanning);

    /** Ligne de référence d'un jour : planning publié (ou clôturé) le plus récent — Bloc 6 §8.1. */
    @Query("select l from PlanningLigneModel l where l.employe.id = :id and l.jour = :jour "
            + "and l.planning.statut in ('PUBLIE', 'CLOTURE') order by l.planning.version desc, l.planning.id desc")
    List<PlanningLigneModel> publieeAuJour(@Param("id") Integer idEmploye, @Param("jour") LocalDate jour);

    /** Lignes à mettre à jour quand un congé est validé ou annulé (RG-PL04, RG-PP01). */
    @Query("select l from PlanningLigneModel l where l.employe.id = :id and l.jour between :debut and :fin "
            + "and l.planning.statut in ('BROUILLON', 'REJETE', 'SOUMIS_RH', 'SOUMIS_FINANCE', 'ARBITRAGE_DG', 'PUBLIE')")
    List<PlanningLigneModel> modifiables(@Param("id") Integer idEmploye, @Param("debut") LocalDate debut,
                                         @Param("fin") LocalDate fin);

    @Query("select l from PlanningLigneModel l where l.employe.id = :id and l.jour between :debut and :fin "
            + "and l.planning.statut in ('PUBLIE', 'CLOTURE') order by l.jour, l.planning.version desc")
    List<PlanningLigneModel> publieesEntre(@Param("id") Integer idEmploye, @Param("debut") LocalDate debut,
                                           @Param("fin") LocalDate fin);
}
