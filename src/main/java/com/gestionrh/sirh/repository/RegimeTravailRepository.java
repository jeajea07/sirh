package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.RegimeTravailModel;

public interface RegimeTravailRepository extends JpaRepository<RegimeTravailModel, Integer> {
    List<RegimeTravailModel> findByActifTrueOrderByLibelleAsc();
}
