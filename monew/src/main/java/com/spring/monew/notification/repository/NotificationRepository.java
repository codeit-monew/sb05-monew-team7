package com.spring.monew.notification.repository;

import com.spring.monew.notification.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

  // Repository: DB 접근 전담.
public interface NotificationRepository
    extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

  // 동일 사용자 소유의 특정 알림을 조회
  Optional<Notification> findByIdAndUserId(UUID notificationId, UUID userId);

  // 사용자의 읽지 않은 알림 존재 여부
  boolean existsByUserIdAndConfirmedFalse(UUID userId);
}