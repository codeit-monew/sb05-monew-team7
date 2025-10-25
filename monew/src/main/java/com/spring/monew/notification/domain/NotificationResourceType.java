package com.spring.monew.notification.domain;


public enum NotificationResourceType {
  INTEREST, COMMENT; // 리소스 타입 (db에는 소문자 "interest" / "comment" 로 저장)

  public static NotificationResourceType from(String value) {
    if (value == null) return null;
    return NotificationResourceType.valueOf(value.trim().toUpperCase());
  }

  public String toDbValue() {
    return name().toLowerCase(); // "interest" | "comment"
  }
}
