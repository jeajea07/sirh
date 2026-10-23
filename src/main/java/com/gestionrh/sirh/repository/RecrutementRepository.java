// Bloc 3
package com.gestionrh.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.RecrutementModel;

public interface RecrutementRepository extends JpaRepository<RecrutementModel, Integer> {
    Optional<RecrutementModel> findBySelection_IdSelection(Integer idSelection);
    Optional<RecrutementModel> findFirstBySelection_IdOffreB2OrderByIdRecrutementAsc(Integer idOffre);
    long countBySelection_IdOffreB2AndEtatRecrutement_Code(Integer idOffre, String codeEtat);
}
