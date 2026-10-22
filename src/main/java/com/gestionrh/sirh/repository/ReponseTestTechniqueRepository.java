package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.ReponseTestTechniqueModel;
import com.gestionrh.sirh.model.TestTechniqueModel;

public interface ReponseTestTechniqueRepository extends JpaRepository<ReponseTestTechniqueModel, Long> {

    List<ReponseTestTechniqueModel> findByTestTechniqueOrderByIdReponseAsc(TestTechniqueModel testTechnique);
}
