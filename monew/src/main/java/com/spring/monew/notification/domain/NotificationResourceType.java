package com.spring.monew.notification.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

public enum NotificationResourceType {
  ARTICLE, COMMENT, LIKE, SUBSCRIPTION;

  @JsonCreator
  public static NotificationResourceType from(String value) {
    if (value == null) throw new IllegalArgumentException("resourceType is required");
    String v = value.trim().toLowerCase(Locale.ROOT);
    if ("interest".equals(v)) return SUBSCRIPTION;
    try {
      return valueOf(v.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported resourceType: " + value);
    }
  }

  @JsonValue
  public String toJson() {
    return switch (this) {
      case SUBSCRIPTION -> "interest";
      case COMMENT -> "comment";
      case ARTICLE -> "article";
      case LIKE -> "like";
    };
  }
}