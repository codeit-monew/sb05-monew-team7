package com.spring.monew.notification.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum NotificationResourceType {
  INTEREST, COMMENT;

  // 잘못된 값이면 400
  @JsonCreator
  public static NotificationResourceType from(String value) {
    if (value == null) throw new IllegalArgumentException("resourceType is required");
    try {
      return valueOf(value.trim().toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported resourceType: " + value);
    }
  }

  //
  @JsonValue
  public String toJson() {
    return name().toLowerCase(Locale.ROOT);
  }
}