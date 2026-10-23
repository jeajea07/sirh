package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.OrganismeModel;

public interface OrganismeRepository extends JpaRepository<OrganismeModel, Integer> {

    List<OrganismeModel> findByActifTrueOrderByNomAsc();

    List<OrganismeModel> findAllByOrderByNomAsc();

    Optional<OrganismeModel> findByCode(String code);
}
