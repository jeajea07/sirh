package com.gestionrh.sirh.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.OnboardingTacheModel;

public interface OnboardingTacheRepository extends JpaRepository<OnboardingTacheModel, Integer> {

    @Query("select t from OnboardingTacheModel t where t.onboarding.id = :id order by t.ordre asc, t.id asc")
    List<OnboardingTacheModel> findByIdOnboardingOrderByOrdreAscIdAsc(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"onboarding", "onboarding.employe", "responsable"})
    @Query("select t from OnboardingTacheModel t where t.responsable.id = :id and t.statut in :statuts order by t.dateEcheance asc")
    List<OnboardingTacheModel> findByIdResponsableAndStatutInOrderByDateEcheanceAsc(
            @Param("id") Integer id, @Param("statuts") Collection<String> statuts);

    @EntityGraph(attributePaths = {"onboarding", "onboarding.employe", "responsable"})
    @Query("select t from OnboardingTacheModel t where t.dateEcheance < :aujourdhui and t.statut not in ('TERMINEE','ANNULEE') order by t.dateEcheance asc")
    List<OnboardingTacheModel> enRetard(@Param("aujourdhui") LocalDate aujourdhui);

    @Query("select count(t) from OnboardingTacheModel t where t.onboarding.id = :id and t.obligatoire = true and t.statut <> :statut")
    long countByIdOnboardingAndObligatoireTrueAndStatutNot(@Param("id") Integer id, @Param("statut") String statut);

    @Query("select count(t) from OnboardingTacheModel t where t.onboarding.id = :id and t.statut = :statut")
    long countByIdOnboardingAndStatut(@Param("id") Integer id, @Param("statut") String statut);

    long countByOnboarding_Id(Integer id);

    @Query("select count(t) from OnboardingTacheModel t where t.responsable.id = :id and t.statut in :statuts")
    long countByIdResponsableAndStatutIn(@Param("id") Integer id, @Param("statuts") Collection<String> statuts);

    @EntityGraph(attributePaths = {"onboarding", "onboarding.employe", "responsable"})
    @Query("select t from OnboardingTacheModel t where t.onboarding.id in :ids")
    List<OnboardingTacheModel> findByIdOnboardingIn(@Param("ids") Collection<Integer> ids);

    @Query("select t from OnboardingTacheModel t where t.onboarding.id = :id and t.dependDeTache is not null")
    List<OnboardingTacheModel> findByIdOnboardingAndDependDeTacheIsNotNull(@Param("id") Integer id);
}
