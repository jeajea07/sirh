package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.ModeleTacheOnboardingModel;

public interface ModeleTacheOnboardingRepository extends JpaRepository<ModeleTacheOnboardingModel, Integer> {

    @Query("select t from ModeleTacheOnboardingModel t where t.modele.id = :id order by t.ordre asc, t.id asc")
    List<ModeleTacheOnboardingModel> findByIdModeleOnboardingOrderByOrdreAscIdAsc(@Param("id") Integer id);

    @Query("select count(t) from ModeleTacheOnboardingModel t where t.modele.id = :id")
    long countByIdModeleOnboarding(@Param("id") Integer id);
}
