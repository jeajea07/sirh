package com.gestionrh.sirh.recrutement.repository;

import com.gestionrh.sirh.recrutement.model.Candidat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {

    Optional<Candidat> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
