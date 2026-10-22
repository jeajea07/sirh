package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.ValidationEtapeModel;

public interface ValidationEtapeRepository extends JpaRepository<ValidationEtapeModel, Integer> {

    List<ValidationEtapeModel> findByIdDemandeBesoinOrderByIdAsc(Integer idDemandeBesoin);

    List<ValidationEtapeModel> findByEtapeAndDecisionIsNullOrderByDateReceptionAsc(String etape);

    List<ValidationEtapeModel> findByEtapeAndIdValidateurAndDecisionIsNotNullOrderByDateDecisionDesc(String etape, Integer idValidateur);
}
