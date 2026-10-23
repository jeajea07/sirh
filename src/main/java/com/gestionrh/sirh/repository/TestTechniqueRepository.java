package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.gestionrh.sirh.model.CandidatureModel;
import com.gestionrh.sirh.model.TestTechniqueModel;

public interface TestTechniqueRepository extends JpaRepository<TestTechniqueModel, Long> {

    @Query("select distinct t from TestTechniqueModel t " +
           "left join fetch t.candidature c " +
           "left join fetch c.candidat cand")
    List<TestTechniqueModel> findAllWithCandidatureAndCandidat();

    Optional<TestTechniqueModel> findByCandidature(CandidatureModel candidature);
}
