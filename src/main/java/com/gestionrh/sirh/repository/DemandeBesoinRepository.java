package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.DemandeBesoinModel;

public interface DemandeBesoinRepository extends JpaRepository<DemandeBesoinModel, Integer> {

    List<DemandeBesoinModel> findByIdDemandeurOrderByIdDesc(Integer idDemandeur);

    @Query("select d from DemandeBesoinModel d where d.departement.id = :departement and d.poste.id = :poste "
            + "and d.id <> :id and d.statut in ('SOUMISE', 'EN_VERIFICATION', 'A_CORRIGER')")
    List<DemandeBesoinModel> demandesSimilairesEnCours(@Param("departement") Integer departement,
                                                  @Param("poste") Integer poste,
                                                  @Param("id") Integer id);

    @Query(value = "SELECT COALESCE(MAX(id_demande_besoin), 0) + 1 FROM demande_besoin", nativeQuery = true)
    Integer prochainNumero();
}
