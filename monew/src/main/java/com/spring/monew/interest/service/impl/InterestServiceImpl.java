package com.spring.monew.interest.service.impl;

import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.interest.service.InterestService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InterestServiceImpl implements InterestService {

  private final InterestRepository interestRepository;

  @Override
  @Transactional
  public InterestDto addInterest(InterestRegisterRequest registerRequest) {
    List<String> similarNames = interestRepository.findSimilarNames(registerRequest.name(), 0.55);

    //예외 처리 필요 interests name exixsts
    if (interestRepository.existsByName(registerRequest.name())) {
      throw new IllegalArgumentException("같은 이름 존재");
    }
    if(!similarNames.isEmpty()) {
      throw new IllegalArgumentException("같은 이름 존재 [유사도 높은 이름]");
    }
    
    Interest interest = interestRepository.save(
        new Interest(registerRequest.name(), registerRequest.keywords()));

    return new InterestDto(
        interest.getId(),
        interest.getName(),
        interest.getKeywords(),
        interest.getSubscriptionsCount(),
        false,
        interest.getCreatedAt());
  }

  @Override
  @Transactional
  public CursorPageResponseInterestDto getInterests(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      Instant after,
      int limit,
      UUID userId
  ) {
    return interestRepository.findCursorPagedInterests(
        keyword, orderBy, direction, cursor, after, limit, userId);
  }

  @Override
  @Transactional
  public InterestDto modifyInterest(UUID interestId, InterestUpdateRequest updateRequest) {
    Interest interest = interestRepository.findById(interestId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 관심사입니다."));

    interest.update(updateRequest.keywords());

    return new InterestDto(
        interest.getId(),
        interest.getName(),
        interest.getKeywords(),
        interest.getSubscriptionsCount(),
        false,
        interest.getCreatedAt());
  }

  @Override
  @Transactional
  public void removeInterest(UUID interestId) {
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 관심사입니다."));

    interestRepository.delete(interest);
  }
}
