package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.enums.recrutement.TypeCritere;
import com.gestionrh.sirh.model.recrutement.CritereOffre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CritereOffreRepository extends JpaRepository<CritereOffre, Integer> {

    List<CritereOffre> findByOffre_IdOffre(Integer idOffre);

    List<CritereOffre> findByOffre_IdOffreAndTypeCritere(Integer idOffre, TypeCritere typeCritere);
}
