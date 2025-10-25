package com.spring.monew.notification.domain;

public enum NotificationResourceType {
  INTEREST, COMMENT; // 리소스 타입 (db에는 소문자 "interest" / "comment" 로 저장)

  public static NotificationResourceType from(String value) {
    if (value == null) return null;
    try {
      return NotificationResourceType.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      // 필요 시 로깅 추가:
      // log.warn("Unknown NotificationResourceType value: {}", value, e);
      return null;
    }
  }

  public String toDbValue() {
    return name().toLowerCase();
  }
  public String toApiValue() {
    return name().toLowerCase();
  }
}