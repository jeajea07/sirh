package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.AnomaliePointageModel;

public interface AnomaliePointageRepository extends JpaRepository<AnomaliePointageModel, Integer> {

    List<AnomaliePointageModel> findByEmploye_IdAndJour(Integer idEmploye, LocalDate jour);

    List<AnomaliePointageModel> findByEmploye_IdOrderByJourDesc(Integer idEmploye);

    List<AnomaliePointageModel> findByStatutInOrderByJourAsc(Collection<String> statuts);

    @Query("select count(a) from AnomaliePointageModel a where a.statut in ('OUVERTE', 'EN_COURS') "
            + "and a.jour between :debut and :fin")
    long aTraiterEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("select a from AnomaliePointageModel a where a.statut = 'OUVERTE' and a.escaladee = false "
            + "and a.dateCreation < :limite")
    List<AnomaliePointageModel> aEscalader(@Param("limite") LocalDateTime limite);
}
