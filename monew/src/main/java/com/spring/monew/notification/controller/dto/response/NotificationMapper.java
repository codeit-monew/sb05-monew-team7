package com.spring.monew.notification.controller.dto.response;

import com.spring.monew.notification.domain.Notification;

public final class NotificationMapper {
  private NotificationMapper() {}
  public static NotificationDto toDto(Notification e) {
    return new NotificationDto(
        e.getId().toString(),
        e.getCreatedAt(),
        e.getUpdatedAt(),
        e.isConfirmed(),
        e.getUserId().toString(),
        e.getContent(),
        e.getResourceType() == null ? null : e.getResourceType().toApiValue(),
        e.getResourceId() == null ? null : e.getResourceId().toString()
    );
  }
}
