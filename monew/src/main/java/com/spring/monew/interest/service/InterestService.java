package com.spring.monew.interest.service;

import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import java.time.Instant;
import java.util.UUID;

public interface InterestService {

  InterestDto addInterest(InterestRegisterRequest registerRequest);

  CursorPageResponseInterestDto getInterests(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      Instant after,
      int limit,
      UUID userId);

  InterestDto modifyInterest(UUID interestId, InterestUpdateRequest updateRequest);

  void removeInterest(UUID interestId);
}
