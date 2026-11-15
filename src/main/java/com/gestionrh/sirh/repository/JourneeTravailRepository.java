package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.JourneeTravailModel;

public interface JourneeTravailRepository extends JpaRepository<JourneeTravailModel, Integer> {

    Optional<JourneeTravailModel> findByEmploye_IdAndJour(Integer idEmploye, LocalDate jour);

    List<JourneeTravailModel> findByEmploye_IdAndJourBetweenOrderByJourAsc(Integer idEmploye, LocalDate debut, LocalDate fin);

    List<JourneeTravailModel> findByJourBetween(LocalDate debut, LocalDate fin);

    @Query("select count(j) > 0 from JourneeTravailModel j where j.jour = :jour and j.statut = 'CLOTUREE'")
    boolean jourCloture(@Param("jour") LocalDate jour);
}
