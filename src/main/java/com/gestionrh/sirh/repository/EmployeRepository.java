package com.gestionrh.sirh.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.EmployeModel;

public interface EmployeRepository extends JpaRepository<EmployeModel, Integer> {

    /** L'employé du compte connecté (le login est l'email, ou le matricule selon la configuration). */
    @Query("select e from EmployeModel e where lower(e.email) = lower(:login) or lower(e.matricule) = lower(:login)")
    Optional<EmployeModel> trouverParLogin(@Param("login") String login);

    List<EmployeModel> findByDepartement_IdAndActifTrueOrderByNomAsc(Integer idDepartement);

    /** Les comptes actifs d'un rôle dans un département (ex. le responsable du département). */
    @Query("select e from EmployeModel e join UtilisateurModel u on u.idEmploye = e.id "
            + "where u.role = :role and u.actif = true and e.actif = true and e.departement.id = :idDepartement")
    List<EmployeModel> validateursDuDepartement(@Param("role") String role, @Param("idDepartement") Integer idDepartement);

    /** Les comptes actifs d'un rôle dans toute l'entreprise (RH, Finance, DG). */
    @Query("select e from EmployeModel e join UtilisateurModel u on u.idEmploye = e.id "
            + "where u.role = :role and u.actif = true and e.actif = true")
    List<EmployeModel> validateursParRole(@Param("role") String role);

    List<EmployeModel> findByActifTrueOrderByNomAsc();

    Optional<EmployeModel> findByEmailIgnoreCase(String email);

    Optional<EmployeModel> findByCin(String cin);

    @Query("select coalesce(max(e.id), 0) from EmployeModel e")
    Integer maxId();

    @EntityGraph(attributePaths = {"departement", "poste"})
    @Query("select e from EmployeModel e where "
            + "(:recherche is null or :recherche = '' or lower(e.nom) like lower(concat('%', :recherche, '%')) "
            + "or lower(e.prenom) like lower(concat('%', :recherche, '%')) "
            + "or lower(e.matricule) like lower(concat('%', :recherche, '%')) "
            + "or lower(e.email) like lower(concat('%', :recherche, '%'))) "
            + "and (:idDepartement is null or e.departement.id = :idDepartement)")
    Page<EmployeModel> rechercher(@Param("recherche") String recherche,
                                  @Param("idDepartement") Integer idDepartement,
                                  Pageable pageable);

    long countByActifTrue();
}
