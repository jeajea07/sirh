package com.gestionrh.sirh.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.UtilisateurModel;

public interface UtilisateurRepository extends JpaRepository<UtilisateurModel, Integer> {

    /** Retrouve le compte à partir de l'email ou du matricule de l'employé. */
    /** Retrouve le compte à partir de l'email de l'employé. */
    @Query(value = """
            SELECT u.*
            FROM utilisateur u
            JOIN employe e ON e.id_employe = u.id_employe
            WHERE LOWER(e.email) = LOWER(:email)
            LIMIT 1
            """, nativeQuery = true)
    Optional<UtilisateurModel> trouverParEmail(@Param("email") String email);
}
