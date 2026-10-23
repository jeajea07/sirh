package com.gestionrh.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.BudgetDepartementModel;

public interface BudgetDepartementRepository extends JpaRepository<BudgetDepartementModel, Integer> {

    Optional<BudgetDepartementModel> findFirstByIdDepartementAndPeriodeStartingWith(Integer idDepartement, String annee);
}
