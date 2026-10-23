package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.model.recrutement.HistoriqueCandidature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoriqueCandidatureRepository extends JpaRepository<HistoriqueCandidature, Integer> {

    List<HistoriqueCandidature> findByCandidature_IdCandidatureOrderByDateActionDesc(Integer idCandidature);
}
