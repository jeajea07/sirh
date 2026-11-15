package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.TypeDocumentAdministratifModel;

public interface TypeDocumentAdministratifRepository extends JpaRepository<TypeDocumentAdministratifModel, Integer> {

    List<TypeDocumentAdministratifModel> findByActifTrueOrderByLibelleAsc();

    List<TypeDocumentAdministratifModel> findByActifTrueAndObligatoireTrueOrderByLibelleAsc();
}
