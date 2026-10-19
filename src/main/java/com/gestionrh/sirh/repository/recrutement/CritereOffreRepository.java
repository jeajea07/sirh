package com.gestionrh.sirh.recrutement.repository;

import com.gestionrh.sirh.recrutement.enums.TypeCritere;
import com.gestionrh.sirh.recrutement.model.CritereOffre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CritereOffreRepository extends JpaRepository<CritereOffre, Integer> {

    List<CritereOffre> findByOffre_IdOffre(Integer idOffre);

    List<CritereOffre> findByOffre_IdOffreAndTypeCritere(Integer idOffre, TypeCritere typeCritere);
}
