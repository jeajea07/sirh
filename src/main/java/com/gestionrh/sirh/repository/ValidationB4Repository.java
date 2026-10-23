package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.ValidationB4Model;

public interface ValidationB4Repository extends JpaRepository<ValidationB4Model, Integer> {

    List<ValidationB4Model> findByTypeObjetAndIdObjetOrderByIdAsc(String type, Integer id);

    Optional<ValidationB4Model> findFirstByTypeObjetAndIdObjetAndDecisionIsNullOrderByIdDesc(String type, Integer id);
}
