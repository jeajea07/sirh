package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.JourFerieModel;

public interface JourFerieRepository extends JpaRepository<JourFerieModel, Integer> {
    boolean existsByJour(LocalDate jour);
    List<JourFerieModel> findByJourBetweenOrderByJourAsc(LocalDate debut, LocalDate fin);
    List<JourFerieModel> findAllByOrderByJourDesc();
}
