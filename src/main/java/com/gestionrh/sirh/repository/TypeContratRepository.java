package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.TypeContratModel;

public interface TypeContratRepository extends JpaRepository<TypeContratModel, Integer> {

    List<TypeContratModel> findAllByOrderByIdAsc();

    Optional<TypeContratModel> findByCode(String code);
}
