package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.TypeCongeModel;

public interface TypeCongeRepository extends JpaRepository<TypeCongeModel, Integer> {
    List<TypeCongeModel> findByActifTrueOrderByLibelleAsc();
    Optional<TypeCongeModel> findByCode(String code);
}
