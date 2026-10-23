// Bloc 3
package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.ReponseCandidatModel;

public interface ReponseCandidatRepository extends JpaRepository<ReponseCandidatModel, Integer> {
    List<ReponseCandidatModel> findByProposition_IdPropositionOrderByReponseLeDesc(Integer idProposition);
}
