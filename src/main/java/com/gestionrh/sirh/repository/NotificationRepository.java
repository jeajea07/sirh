package com.gestionrh.sirh.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionrh.sirh.model.NotificationModel;

public interface NotificationRepository extends JpaRepository<NotificationModel, Integer> {
}
