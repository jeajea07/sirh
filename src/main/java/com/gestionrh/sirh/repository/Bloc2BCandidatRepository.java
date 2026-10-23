package com.gestionrh.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.CandidatModel;

public interface Bloc2BCandidatRepository extends JpaRepository<CandidatModel, Long> {

    Optional<CandidatModel> findByEmail(String email);

    Optional<CandidatModel> findByIdentifiant(String identifiant);
}
