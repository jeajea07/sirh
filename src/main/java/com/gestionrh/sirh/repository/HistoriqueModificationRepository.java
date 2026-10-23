package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.HistoriqueModificationModel;

public interface HistoriqueModificationRepository extends JpaRepository<HistoriqueModificationModel, Integer> {

    List<HistoriqueModificationModel> findByIdDemandeBesoinOrderByIdAsc(Integer idDemandeBesoin);

    Optional<HistoriqueModificationModel> findFirstByIdDemandeBesoinOrderByIdAsc(Integer idDemandeBesoin);

    Optional<HistoriqueModificationModel> findFirstByIdDemandeBesoinAndRemarqueContainingOrderByIdDesc(
            Integer idDemandeBesoin, String texte);
}
