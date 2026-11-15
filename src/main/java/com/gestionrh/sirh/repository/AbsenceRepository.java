package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.AbsenceModel;

public interface AbsenceRepository extends JpaRepository<AbsenceModel, Integer> {

    List<AbsenceModel> findByEmploye_IdOrderByDateDebutDesc(Integer idEmploye);

    List<AbsenceModel> findByStatutInOrderByDateDebutAsc(Collection<String> statuts);

    @Query("select a from AbsenceModel a where a.employe.id = :id and a.statut in ('DECLAREE', 'EN_ATTENTE_JUSTIFICATIF', "
            + "'JUSTIFIEE', 'INJUSTIFIEE') and a.dateDebut <= :fin and a.dateFin >= :debut")
    List<AbsenceModel> activesEntre(@Param("id") Integer idEmploye, @Param("debut") LocalDate debut,
                                    @Param("fin") LocalDate fin);

    @Query("select a from AbsenceModel a where a.statut in ('DECLAREE', 'EN_ATTENTE_JUSTIFICATIF', 'JUSTIFIEE', 'INJUSTIFIEE') "
            + "and a.dateDebut <= :fin and a.dateFin >= :debut order by a.dateDebut")
    List<AbsenceModel> toutesActivesEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("select a from AbsenceModel a where a.statut = 'DECLAREE' and a.dateLimiteJustificatif is not null "
            + "and a.dateLimiteJustificatif < :jour")
    List<AbsenceModel> justificatifEnRetard(@Param("jour") LocalDate jour);
}
