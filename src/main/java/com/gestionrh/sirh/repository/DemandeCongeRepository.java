package com.gestionrh.sirh.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.DemandeCongeModel;

public interface DemandeCongeRepository extends JpaRepository<DemandeCongeModel, Integer> {

    List<DemandeCongeModel> findByEmploye_IdOrderByDateDebutDesc(Integer idEmploye);

    List<DemandeCongeModel> findByStatutInOrderByDateDebutAsc(Collection<String> statuts);

    List<DemandeCongeModel> findByManager_IdAndStatutOrderByDateDebutAsc(Integer idManager, String statut);

    /** Jours réservés : demandes en cours de circuit ou validées non encore prises (RG-SC05). */
    @Query("select coalesce(sum(d.nbJours), 0) from DemandeCongeModel d where d.employe.id = :id "
            + "and d.type.typeSolde = :solde and d.statut in ('SOUMISE', 'VALIDEE_MANAGER', 'VALIDEE', 'EN_COURS')")
    BigDecimal reserve(@Param("id") Integer idEmploye, @Param("solde") String typeSolde);

    /** RG-CH01 : demandes actives qui touchent la période (contrôle fin à la demi-journée dans le service). */
    @Query("select d from DemandeCongeModel d where d.employe.id = :id and d.statut in :statuts "
            + "and d.dateDebut <= :fin and d.dateFin >= :debut and (:exclu is null or d.id <> :exclu)")
    List<DemandeCongeModel> actives(@Param("id") Integer idEmploye, @Param("debut") LocalDate debut,
                                    @Param("fin") LocalDate fin, @Param("statuts") Collection<String> statuts,
                                    @Param("exclu") Integer exclu);

    /** Congés validés couvrant une période (calcul du temps, planning, paie). */
    @Query("select d from DemandeCongeModel d where d.employe.id = :id "
            + "and d.statut in ('VALIDEE', 'EN_COURS', 'PRISE', 'INTERROMPUE') and d.dateDebut <= :fin and d.dateFin >= :debut")
    List<DemandeCongeModel> valideesEntre(@Param("id") Integer idEmploye, @Param("debut") LocalDate debut,
                                          @Param("fin") LocalDate fin);

    @Query("select d from DemandeCongeModel d where d.statut in ('VALIDEE', 'EN_COURS', 'PRISE', 'INTERROMPUE') "
            + "and d.dateDebut <= :fin and d.dateFin >= :debut order by d.dateDebut")
    List<DemandeCongeModel> toutesValideesEntre(@Param("debut") LocalDate debut, @Param("fin") LocalDate fin);

    @Query("select d from DemandeCongeModel d where d.statut = 'SOUMISE' and d.escaladee = false and d.dateCreation < :limite")
    List<DemandeCongeModel> aEscalader(@Param("limite") LocalDateTime limite);

    @Query("select count(d) from DemandeCongeModel d where d.reference like concat(:prefixe, '%')")
    long compterReferences(@Param("prefixe") String prefixe);
}
