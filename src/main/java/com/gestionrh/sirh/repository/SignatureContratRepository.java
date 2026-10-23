package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.SignatureContratModel;

public interface SignatureContratRepository extends JpaRepository<SignatureContratModel, Integer> {

    @Query("select s from SignatureContratModel s where s.contrat.id = :id order by s.dateSignature asc")
    List<SignatureContratModel> findByContrat_IdOrderByDateSignatureAsc(@Param("id") Integer id);

    @Query("select s from SignatureContratModel s where s.contrat.id = :id and s.partie = :partie")
    Optional<SignatureContratModel> findByIdContratAndPartie(@Param("id") Integer id,
                                                              @Param("partie") String partie);

    @Query("select count(s) > 0 from SignatureContratModel s where s.contrat.id = :id "
            + "and s.partie = :partie and s.statut = :statut")
    boolean existsByIdContratAndPartieAndStatut(@Param("id") Integer id,
                                                 @Param("partie") String partie,
                                                 @Param("statut") String statut);

    @Query("select count(s) from SignatureContratModel s where s.contrat.id = :id and s.statut = :statut")
    long countByIdContratAndStatut(@Param("id") Integer id, @Param("statut") String statut);
}
