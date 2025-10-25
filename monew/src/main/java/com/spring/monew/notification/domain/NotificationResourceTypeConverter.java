package com.spring.monew.notification.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class NotificationResourceTypeConverter implements AttributeConverter<NotificationResourceType, String> {

  @Override
  public String convertToDatabaseColumn(NotificationResourceType attribute) {
    return attribute == null ? null : attribute.toDbValue(); // 소문자 저장
  }

  @Override
  public NotificationResourceType convertToEntityAttribute(String dbData) {
    return dbData == null ? null : NotificationResourceType.from(dbData);
  }
}
