package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.VariablePaieModel;

public interface VariablePaieRepository extends JpaRepository<VariablePaieModel, Integer> {

    @Query("select v from VariablePaieModel v where v.periode.id = :id order by v.employe.nom, v.source, v.code")
    List<VariablePaieModel> dePeriode(@Param("id") Integer idPeriode);
}
