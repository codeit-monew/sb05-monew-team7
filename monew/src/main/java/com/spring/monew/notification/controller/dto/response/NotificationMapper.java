package com.spring.monew.notification.controller.dto.response;

import lombok.NonNull;
import com.spring.monew.notification.domain.Notification;

public final class NotificationMapper {

  private NotificationMapper() {
  }

  public static NotificationDto toDto(@NonNull Notification e) {
    {
      return new NotificationDto(
          e.getId(),           // UUID
          e.getCreatedAt(),    // Instant
          e.getUpdatedAt(),    // Instant
          e.isConfirmed(),
          e.getUserId(),       // UUID
          e.getContent(),
          e.getResourceType(), // ENUM
          e.getResourceId()    // UUID
      );
    }
  }
}