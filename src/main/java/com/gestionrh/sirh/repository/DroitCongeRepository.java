package com.gestionrh.sirh.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.DroitCongeModel;

public interface DroitCongeRepository extends JpaRepository<DroitCongeModel, Integer> {

    boolean existsByEmploye_IdAndTypeSoldeAndPeriodeReferenceAndOrigine(Integer idEmploye, String typeSolde,
                                                                        LocalDate periode, String origine);

    /** La reprise de l'existant couvre-t-elle déjà ce mois ? (évite une double acquisition) */
    @Query("select count(d) > 0 from DroitCongeModel d where d.employe.id = :id and d.typeSolde = 'CONGE_PAYE' "
            + "and d.origine = 'REPRISE' and d.periodeReference >= :mois")
    boolean repriseCouvre(@Param("id") Integer idEmploye, @Param("mois") LocalDate mois);
}
