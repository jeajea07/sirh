package com.gestionrh.sirh.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.PointageModel;

public interface PointageRepository extends JpaRepository<PointageModel, Integer> {

    @Query("select p from PointageModel p where p.employe.id = :id and p.statut = 'VALIDE' "
            + "and p.horodatage >= :debut and p.horodatage < :fin order by p.horodatage asc")
    List<PointageModel> valides(@Param("id") Integer idEmploye, @Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin);
}
