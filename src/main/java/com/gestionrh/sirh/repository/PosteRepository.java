package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.PosteModel;

public interface PosteRepository extends JpaRepository<PosteModel, Integer> {

    List<PosteModel> findAllByOrderByDesignationAsc();
}
