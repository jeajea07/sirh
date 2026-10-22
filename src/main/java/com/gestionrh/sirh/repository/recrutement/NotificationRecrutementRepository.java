package com.gestionrh.sirh.repository.recrutement;

import com.gestionrh.sirh.enums.recrutement.StatutEnvoi;
import com.gestionrh.sirh.model.recrutement.NotificationRecrutement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecrutementRepository extends JpaRepository<NotificationRecrutement, Integer> {

    List<NotificationRecrutement> findByIdDestinataireEmployeAndLuFalseOrderByDateCreationDesc(Integer idEmploye);

    long countByIdDestinataireEmployeAndLuFalse(Integer idEmploye);

    List<NotificationRecrutement> findByStatutEnvoi(StatutEnvoi statutEnvoi);

    /** Evite d'envoyer deux fois la meme notification pour une candidature. */
    boolean existsByIdCandidatureAndTypeNotification(Integer idCandidature, String typeNotification);
}
