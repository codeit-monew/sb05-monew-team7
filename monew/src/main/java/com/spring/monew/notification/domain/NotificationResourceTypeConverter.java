package com.spring.monew.notification.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = false)
public class NotificationResourceTypeConverter implements AttributeConverter<NotificationResourceType, String> {

    private static final Logger log = LoggerFactory.getLogger(NotificationResourceTypeConverter.class);

    @Override
    public String convertToDatabaseColumn(NotificationResourceType attribute) {
        return attribute == null ? null : attribute.toDbValue(); // 소문자 저장
    }

    @Override
    public NotificationResourceType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        var parsed = NotificationResourceType.from(dbData);
        if (parsed == null) {
            log.warn("Invalid resource_type in DB: {}", dbData);
        }
        return parsed;
    }
}