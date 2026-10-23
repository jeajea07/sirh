package com.gestionrh.sirh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.gestionrh.sirh.model.NotificationModel;

public interface NotificationRepository extends JpaRepository<NotificationModel, Integer> {

    List<NotificationModel> findTop10ByIdDestinataireOrderByIdDesc(Integer idDestinataire);

    long countByIdDestinataireAndLueFalse(Integer idDestinataire);

    @Modifying
    @Query("update NotificationModel n set n.lue = true where n.idDestinataire = :id and n.lue = false")
    int marquerToutesLues(@Param("id") Integer idDestinataire);
}
