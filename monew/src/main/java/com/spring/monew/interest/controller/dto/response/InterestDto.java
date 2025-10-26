package com.spring.monew.interest.controller.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import java.util.UUID;

public record InterestDto(
    UUID id,
    String name,
    List<String> keywords,
    long subscriberCount,
    boolean subscribedByMe
) {
  @QueryProjection  //QueryDSL용
  public InterestDto {}
}
