package com.gestionrh.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.CandidatureModel;
import com.gestionrh.sirh.model.TestTechniqueModel;

public interface TestTechniqueRepository extends JpaRepository<TestTechniqueModel, Long> {

    Optional<TestTechniqueModel> findByCandidature(CandidatureModel candidature);
}
