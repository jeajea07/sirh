package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.ModeleHoraireModel;

public interface ModeleHoraireRepository extends JpaRepository<ModeleHoraireModel, Integer> {
    List<ModeleHoraireModel> findByActifTrueOrderByHeureDebutAsc();
    List<ModeleHoraireModel> findAllByOrderByCodeAsc();
    Optional<ModeleHoraireModel> findByCode(String code);
}
