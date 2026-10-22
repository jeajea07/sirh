package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.CircuitValidationModel;

public interface CircuitValidationRepository extends JpaRepository<CircuitValidationModel, Integer> {

    List<CircuitValidationModel> findAllByOrderByIdAsc();
}
