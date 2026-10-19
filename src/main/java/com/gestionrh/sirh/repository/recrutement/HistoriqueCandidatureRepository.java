package com.gestionrh.sirh.recrutement.repository;

import com.gestionrh.sirh.recrutement.model.HistoriqueCandidature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueCandidatureRepository extends JpaRepository<HistoriqueCandidature, Integer> {

    List<HistoriqueCandidature> findByCandidature_IdCandidatureOrderByDateActionDesc(Integer idCandidature);
}
