package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.model.recrutement.Candidat;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidatRepository extends JpaRepository<Candidat, Integer> {

    Optional<Candidat> findByEmailIgnoreCase(String email);

    Optional<Candidat> findByIdentifiant(String identifiant);

    boolean existsByEmailIgnoreCase(String email);

    /** Sérialise les créations concurrentes pour la même adresse, en PostgreSQL. */
    @Query(value = "SELECT pg_advisory_xact_lock(hashtextextended(LOWER(:email), 0)) IS NULL", nativeQuery = true)
    boolean verrouillerEmailPourCreation(String email);
}
