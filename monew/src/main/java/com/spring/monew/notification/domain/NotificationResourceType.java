package com.spring.monew.notification.domain;

public enum NotificationResourceType {
  INTEREST, COMMENT;
  public static NotificationResourceType from(String value) {
    if (value == null) return null;
    try {
      return NotificationResourceType.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  public String toDbValue() { return name().toLowerCase(); }

  public String toApiValue() { return name().toLowerCase(); }
}
