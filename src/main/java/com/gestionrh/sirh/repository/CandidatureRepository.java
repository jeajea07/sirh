package com.gestionrh.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.CandidatureModel;

public interface CandidatureRepository extends JpaRepository<CandidatureModel, Long> {

    Optional<CandidatureModel> findByCandidat_IdCandidat(Long idCandidat);
}
