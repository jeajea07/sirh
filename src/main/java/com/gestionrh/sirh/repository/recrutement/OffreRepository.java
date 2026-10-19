package com.gestionrh.sirh.recrutement.repository;

import com.gestionrh.sirh.recrutement.enums.OffreStatut;
import com.gestionrh.sirh.recrutement.model.Offre;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OffreRepository extends JpaRepository<Offre, Integer> {

    Optional<Offre> findByReference(String reference);

    Page<Offre> findByStatut(OffreStatut statut, Pageable pageable);

    Page<Offre> findByTitreContainingIgnoreCase(String titre, Pageable pageable);

    Page<Offre> findByStatutAndTitreContainingIgnoreCase(OffreStatut statut, String titre, Pageable pageable);

    /** Offres publiees dont la date limite est depassee (arret automatique des candidatures). */
    List<Offre> findByStatutAndDateLimiteBefore(OffreStatut statut, LocalDate date);

    /** Regle : une demande ne donne pas plusieurs offres actives (ex. statuts exclus : ANNULEE, TERMINEE). */
    boolean existsByIdDemandeBesoinAndStatutNotIn(Integer idDemandeBesoin, Collection<OffreStatut> statuts);
}
