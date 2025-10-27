package com.spring.monew.notification.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum NotificationResourceType {
  INTEREST, ARTICLE, COMMENT, SUBSCRIPTION;

  private static final Logger log = LoggerFactory.getLogger(NotificationResourceType.class);

  // 소문자 키 → enum 매핑용
  private static final Map<String, NotificationResourceType> LOOKUP =
      Stream.of(values()).collect(Collectors.toUnmodifiableMap(
          t -> t.name().toLowerCase(Locale.ROOT),
          t -> t
      ));

  // 약함: 알 수 없는 값이면 null 반환(호출부에서 null 처리).
  public static NotificationResourceType from(String value) {
    if (value == null) return null;
    String key = value.trim().toLowerCase(Locale.ROOT);
    NotificationResourceType parsed = LOOKUP.get(key);
    if (parsed == null) {
      log.warn("Unknown NotificationResourceType value: {}", value);
    }
    return parsed;
  }

  // 강함: 알 수 없는 값이면 IllegalArgumentException
  public static NotificationResourceType fromStrict(String value) {
    if (value == null) return null;
    String key = value.trim().toLowerCase(Locale.ROOT);
    NotificationResourceType parsed = LOOKUP.get(key);
    if (parsed == null) {
      throw new IllegalArgumentException("Unsupported NotificationResourceType: " + value);
    }
    return parsed;
  }

  // DB 저장용 문자열(소문자)
  public String toDbValue() {
    return toLowerName();
  }

  // API 응답용 문자열(소문자)
  public String toApiValue() {
    return toLowerName();
  }

  private String toLowerName() {
    return name().toLowerCase(Locale.ROOT);
  }
}