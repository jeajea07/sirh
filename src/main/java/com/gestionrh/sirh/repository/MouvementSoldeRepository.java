package com.gestionrh.sirh.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.MouvementSoldeModel;

public interface MouvementSoldeRepository extends JpaRepository<MouvementSoldeModel, Integer> {

    @Query("select coalesce(sum(m.jours), 0) from MouvementSoldeModel m where m.employe.id = :id "
            + "and m.typeSolde = :solde and m.typeMouvement in :types")
    BigDecimal somme(@Param("id") Integer idEmploye, @Param("solde") String typeSolde,
                     @Param("types") List<String> types);

    @Query("select m from MouvementSoldeModel m where m.employe.id = :id order by m.dateMouvement desc")
    List<MouvementSoldeModel> historique(@Param("id") Integer idEmploye);

    boolean existsByIdDemandeCongeAndTypeMouvement(Integer idDemande, String typeMouvement);
}
