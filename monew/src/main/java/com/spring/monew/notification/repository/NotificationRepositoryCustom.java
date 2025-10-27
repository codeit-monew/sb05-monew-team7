package com.spring.monew.notification.repository;

import com.spring.monew.notification.domain.Notification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {

  List<Notification> findUnreadByUserIdWithCursor(
      UUID userId,
      Instant afterOrNow,
      Instant cursorCreatedAt,
      UUID cursorId,
      int limitPlusOne
  );

  long confirmAllByUserId(UUID userId);
  long deleteConfirmedBefore(Instant threshold);

  Instant getDatabaseNow();
}
