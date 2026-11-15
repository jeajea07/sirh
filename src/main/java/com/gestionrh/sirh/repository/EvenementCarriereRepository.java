package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.EvenementCarriereModel;

public interface EvenementCarriereRepository extends JpaRepository<EvenementCarriereModel, Integer> {

    /** RG-HI03 : chronologie triée par date d'effet. */
    @EntityGraph(attributePaths = {"auteur"})
    @Query("select e from EvenementCarriereModel e where e.employe.id = :id "
            + "order by e.dateEffet desc, e.dateEnregistrement desc")
    List<EvenementCarriereModel> chronologie(@Param("id") Integer idEmploye);

    boolean existsByObjetSourceAndIdObjetSourceAndTypeEvenement(String objetSource, Integer idObjetSource,
                                                                String typeEvenement);
}
