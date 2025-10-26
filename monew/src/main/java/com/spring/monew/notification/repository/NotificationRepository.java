package com.spring.monew.notification.repository;

import com.spring.monew.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository
    extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

  Optional<Notification> findByIdAndUserId(UUID id, UUID userId);
}
