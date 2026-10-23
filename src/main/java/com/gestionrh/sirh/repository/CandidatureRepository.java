package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.CandidatureModel;

public interface CandidatureRepository extends JpaRepository<CandidatureModel, Long> {

    Optional<CandidatureModel> findByCandidat_IdCandidat(Long idCandidat);

    @Query("select distinct c from CandidatureModel c " +
           "left join fetch c.candidat cand " +
           "left join fetch c.testTechnique t " +
           "where cand.idCandidat = :idCandidat")
    List<CandidatureModel> findAllWithCandidatAndTestByIdCandidat(@Param("idCandidat") Long idCandidat);
}
