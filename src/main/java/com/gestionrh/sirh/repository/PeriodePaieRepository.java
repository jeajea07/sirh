package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.PeriodePaieModel;

public interface PeriodePaieRepository extends JpaRepository<PeriodePaieModel, Integer> {
    Optional<PeriodePaieModel> findByAnneeAndMois(int annee, int mois);
    List<PeriodePaieModel> findAllByOrderByAnneeDescMoisDesc();
    boolean existsByAnneeAndMoisAndStatut(int annee, int mois, String statut);
}
