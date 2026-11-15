package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.RegimeSalarieModel;

public interface RegimeSalarieRepository extends JpaRepository<RegimeSalarieModel, Integer> {

    @Query("select r from RegimeSalarieModel r where r.employe.id = :id and r.dateDebut <= :jour "
            + "and (r.dateFin is null or r.dateFin >= :jour) order by r.dateDebut desc")
    List<RegimeSalarieModel> auJour(@Param("id") Integer idEmploye, @Param("jour") LocalDate jour);

    @Query("select r from RegimeSalarieModel r where r.employe.id = :id and r.dateFin is null")
    Optional<RegimeSalarieModel> enCours(@Param("id") Integer idEmploye);
}
