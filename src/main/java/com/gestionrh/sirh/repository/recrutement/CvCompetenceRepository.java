package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.model.recrutement.CvCompetence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvCompetenceRepository extends JpaRepository<CvCompetence, Integer> {

    List<CvCompetence> findByCandidature_IdCandidature(Integer idCandidature);
}
