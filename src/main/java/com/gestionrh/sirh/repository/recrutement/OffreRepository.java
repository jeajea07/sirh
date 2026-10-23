package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.enums.recrutement.OffreStatut;
import com.gestionrh.sirh.model.recrutement.Offre;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OffreRepository extends JpaRepository<Offre, Integer> {

    Optional<Offre> findByReference(String reference);

    Optional<Offre> findByIdDemandeBesoinAndStatutNotIn(Integer idDemandeBesoin, Collection<OffreStatut> statuts);

    Page<Offre> findByStatut(OffreStatut statut, Pageable pageable);

    Page<Offre> findByTitreContainingIgnoreCase(String titre, Pageable pageable);

    Page<Offre> findByStatutAndTitreContainingIgnoreCase(OffreStatut statut, String titre, Pageable pageable);

    Page<Offre> findByStatutAndDateLimiteGreaterThanEqual(OffreStatut statut, LocalDate date, Pageable pageable);

    Page<Offre> findByStatutAndDateLimiteGreaterThanEqualAndTitreContainingIgnoreCase(
            OffreStatut statut, LocalDate date, String titre, Pageable pageable);

    /** Offres publiees dont la date limite est depassee (arret automatique des candidatures). */
    List<Offre> findByStatutAndDateLimiteBefore(OffreStatut statut, LocalDate date);

    /** Regle : une demande ne donne pas plusieurs offres actives (ex. statuts exclus : ANNULEE, TERMINEE). */
    boolean existsByIdDemandeBesoinAndStatutNotIn(Integer idDemandeBesoin, Collection<OffreStatut> statuts);
}
