// Bloc 3
package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.SelectionModel;

public interface SelectionRepository extends JpaRepository<SelectionModel, Integer> {
    boolean existsByIdCandidatureB2(Integer idCandidatureB2);

    @Query("select s from SelectionModel s where (:id is null or s.idCandidatureB2 = :id "
            + "or s.idOffreB2 = :id or s.idCandidatB2 = :id)")
    Page<SelectionModel> rechercher(@Param("id") Integer id, Pageable pageable);
}
