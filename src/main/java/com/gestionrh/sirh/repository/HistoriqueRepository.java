// Bloc 3
package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.HistoriqueModel;

public interface HistoriqueRepository extends JpaRepository<HistoriqueModel, Integer> {
    List<HistoriqueModel> findByTypeElementAndIdElementOrderByDateActionDesc(String typeElement, Integer idElement);
}
