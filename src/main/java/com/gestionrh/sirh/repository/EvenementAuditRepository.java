package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.EvenementAuditModel;

public interface EvenementAuditRepository extends JpaRepository<EvenementAuditModel, Integer> {

    @EntityGraph(attributePaths = {"auteur"})
    List<EvenementAuditModel> findByTypeObjetAndIdObjetOrderByDateEvenementDesc(String type, Integer id);

    @EntityGraph(attributePaths = {"auteur"})
    Page<EvenementAuditModel> findAllByOrderByDateEvenementDesc(Pageable pageable);
}
