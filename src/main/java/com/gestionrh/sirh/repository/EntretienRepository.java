package com.gestionrh.sirh.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.EntretienModel;

public interface EntretienRepository extends JpaRepository<EntretienModel, Integer> {

    @EntityGraph(attributePaths = "notes")
    List<EntretienModel> findByIdCandidatureOrderByNiveauAsc(Integer idCandidature);

    @EntityGraph(attributePaths = "notes")
    Optional<EntretienModel> findWithNotesById(Integer id);

    Optional<EntretienModel> findByIdCandidatureAndNiveau(Integer idCandidature, Short niveau);

    List<EntretienModel> findByIdCandidatureIn(Collection<Integer> idsCandidature);
}
