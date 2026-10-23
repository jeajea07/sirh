package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.enums.recrutement.StatutEnvoi;
import com.gestionrh.sirh.model.recrutement.NotificationRecrutement;
import java.util.List;
import java.util.Collection;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecrutementRepository extends JpaRepository<NotificationRecrutement, Integer> {

    List<NotificationRecrutement> findByIdDestinataireEmployeAndLuFalseOrderByDateCreationDesc(Integer idEmploye);

    List<NotificationRecrutement> findByIdDestinataireEmployeOrderByLuAscDateCreationDesc(Integer idEmploye);

    long countByIdDestinataireEmployeAndLuFalse(Integer idEmploye);

    List<NotificationRecrutement> findByStatutEnvoi(StatutEnvoi statutEnvoi);

    List<NotificationRecrutement> findByStatutEnvoiInAndTentativesLessThanOrderByDateCreationAsc(
            Collection<StatutEnvoi> statuts, Integer limiteTentatives);

    /** Evite d'envoyer deux fois la meme notification pour une candidature. */
    boolean existsByIdCandidatureAndTypeNotification(Integer idCandidature, String typeNotification);

    boolean existsByIdDestinataireEmployeAndTypeNotificationAndDateCreationBetween(
            Integer idEmploye, String typeNotification, LocalDateTime depuis, LocalDateTime avant);
}
