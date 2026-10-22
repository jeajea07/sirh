package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.GrilleSalarialeModel;

public interface GrilleSalarialeRepository extends JpaRepository<GrilleSalarialeModel, Integer> {

    @Query("select g from GrilleSalarialeModel g where g.poste.id = :poste "
            + "and g.typeContrat.id = :typeContrat "
            + "and (g.departement is null or g.departement.id = :departement)")
    List<GrilleSalarialeModel> candidats(@Param("poste") Integer poste, @Param("departement") Integer departement,
                                         @Param("typeContrat") Integer typeContrat);
}
