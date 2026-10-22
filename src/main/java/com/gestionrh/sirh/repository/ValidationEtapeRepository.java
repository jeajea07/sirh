package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.ValidationEtapeModel;

public interface ValidationEtapeRepository extends JpaRepository<ValidationEtapeModel, Integer> {

    List<ValidationEtapeModel> findByIdDemandeBesoinOrderByIdAsc(Integer idDemandeBesoin);
}
