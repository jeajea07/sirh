package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.model.recrutement.Candidat;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {

    Optional<Candidat> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
