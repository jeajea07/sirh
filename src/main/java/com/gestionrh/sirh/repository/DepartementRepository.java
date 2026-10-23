package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.DepartementModel;

public interface DepartementRepository extends JpaRepository<DepartementModel, Integer> {

    List<DepartementModel> findAllByOrderByDesignationAsc();
}
