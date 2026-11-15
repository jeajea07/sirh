package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.HistoriqueSalaireModel;

public interface HistoriqueSalaireRepository extends JpaRepository<HistoriqueSalaireModel, Integer> {

    @Query("select h from HistoriqueSalaireModel h where h.employe.id = :id order by h.dateEffet desc, h.id desc")
    List<HistoriqueSalaireModel> historique(@Param("id") Integer idEmploye);

    @Query("select h from HistoriqueSalaireModel h where h.employe.id = :id and h.dateFin is null")
    Optional<HistoriqueSalaireModel> enCours(@Param("id") Integer idEmploye);
}
