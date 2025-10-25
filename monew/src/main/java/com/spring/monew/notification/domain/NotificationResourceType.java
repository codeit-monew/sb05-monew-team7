package com.spring.monew.notification.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public enum NotificationResourceType {
  INTEREST, COMMENT;

  private static final Logger log = LoggerFactory.getLogger(NotificationResourceType.class);

  public static NotificationResourceType from(String value) {
    if (value == null) return null;
    try {
      return NotificationResourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      // 운영에서 모니터링 가능하도록 경고 남김 (필요 시 error/metrics로 승격)
      log.warn("Unknown NotificationResourceType value: {}", value);
      return null;
    }
  }

  public static NotificationResourceType fromStrict(String value) {
    if (value == null) return null;
    // 실패 시 그대로 IllegalArgumentException 전파
    return NotificationResourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
  }

  public String toDbValue() {
    return name().toLowerCase(Locale.ROOT);
  }

  public String toApiValue() {
    return name().toLowerCase(Locale.ROOT);
  }
}