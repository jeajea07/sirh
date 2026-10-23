package com.gestionrh.sirh.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.DeclarationSocialeModel;

public interface DeclarationSocialeRepository extends JpaRepository<DeclarationSocialeModel, Integer> {

    @EntityGraph(attributePaths = {"employe", "organisme", "documentJustificatif"})
    Page<DeclarationSocialeModel> findByStatut(String statut, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"employe", "organisme", "documentJustificatif"})
    Page<DeclarationSocialeModel> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"employe", "organisme", "documentJustificatif"})
    @Query("select d from DeclarationSocialeModel d where d.employe.id = :id order by d.dateCreation desc")
    List<DeclarationSocialeModel> findByIdEmployeOrderByDateCreationDesc(@Param("id") Integer id);

    long countByStatutIn(Collection<String> statuts);
}
