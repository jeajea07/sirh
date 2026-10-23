package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gestionrh.sirh.model.TestTechniqueModel;
import com.gestionrh.sirh.model.recrutement.Candidature;

public interface TestTechniqueRepository extends JpaRepository<TestTechniqueModel, Integer> {

    @Query("select distinct t from TestTechniqueModel t " +
           "left join fetch t.candidature c " +
           "left join fetch c.candidat cand")
    List<TestTechniqueModel> findAllWithCandidatureAndCandidat();

    Optional<TestTechniqueModel> findByCandidature(Candidature candidature);

    Optional<TestTechniqueModel> findByCandidature_IdCandidature(Integer idCandidature);
}
