// Bloc 3
package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.gestionrh.sirh.model.ReferenceModel;

@NoRepositoryBean
public interface ReferenceRepository<T extends ReferenceModel> extends JpaRepository<T, Integer> {
    Optional<T> findByCode(String code);
    List<T> findAllByOrderByLibelleAsc();
}
