// Bloc 3
package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.PropositionModel;

public interface PropositionRepository extends JpaRepository<PropositionModel, Integer> {
    List<PropositionModel> findBySelection_IdSelectionOrderByNumeroVersionDesc(Integer idSelection);
    Optional<PropositionModel> findFirstBySelection_IdSelectionOrderByNumeroVersionDesc(Integer idSelection);
}
