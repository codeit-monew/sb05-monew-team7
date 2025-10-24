package com.spring.monew.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;

@Converter
public class KeywordsConverter implements AttributeConverter<List<String>, String> {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(List<String> attribute) {
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new IllegalArgumentException("리스트를 JSON으로 변환 실패", e);
    }
  }

  @Override
  public List<String> convertToEntityAttribute(String dbData) {
    try {
      if (dbData == null || dbData.isBlank()) return new ArrayList<>();

      return objectMapper.readValue(dbData, objectMapper.getTypeFactory()
          .constructCollectionType(List.class, String.class));
    } catch (Exception e) {
      throw new IllegalArgumentException("JSON을 리스트로 변환 실패", e);
    }
  }
}
