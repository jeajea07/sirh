package com.gestionrh.sirh.recrutement.repository;

import com.gestionrh.sirh.recrutement.model.CvCompetence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvCompetenceRepository extends JpaRepository<CvCompetence, Integer> {

    List<CvCompetence> findByCandidature_IdCandidature(Integer idCandidature);
}
