package com.spring.monew.common.config.converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToInstantConverter implements Converter<String, Instant> {

  @Override
  public Instant convert(String source) {
    if (source == null || source.isBlank()) {
      return null;
    }

    try {
      return Instant.parse(source);
    } catch (Exception e) {
      try {
        LocalDateTime localDateTime = LocalDateTime.parse(source, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return localDateTime.toInstant(ZoneOffset.UTC);
      } catch (Exception ex) {
        throw new IllegalArgumentException("Invalid date format: " + source + ". Expected ISO-8601 format like 2024-10-23T00:00:00 or 2024-10-23T00:00:00Z");
      }
    }
  }
}
