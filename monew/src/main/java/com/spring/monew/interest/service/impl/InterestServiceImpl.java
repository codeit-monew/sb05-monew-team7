package com.spring.monew.interest.service.impl;

import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.interest.service.InterestService;
import java.time.Instant;
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
    //예외 처리 필요 interests name exixsts
    if (interestRepository.existsByName(registerRequest.name())) {
      throw new IllegalArgumentException("같은 이름 존재");
    }
    Interest save = interestRepository.save(
        new Interest(registerRequest.name(), registerRequest.keywords()));
    return new InterestDto(
        save.getId(),
        save.getName(),
        save.getKeywords(),
        save.getSubscriptionsCount(),
        true);  //일단 트루 처리
  }

  @Override
  public CursorPageResponseInterestDto getInterests(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      Instant after,
      int limit,
      UUID userId
  ) {
    // 예외 처리 필요 400, 500
    // 유사도 80% 로직 필요
    // repository 동적 쿼리 or QueryDSL builder
    return interestRepository.findCursorPagedInterests(
        keyword, orderBy, direction, cursor, after, limit, userId);
  }

  @Override
  public InterestDto modifyInterest(UUID interestId, InterestUpdateRequest updateRequest) {
    Interest interest = interestRepository.findById(interestId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 관심사입니다."));

    interest.update(updateRequest.keywords());


    return new InterestDto(
        interest.getId(),
        interest.getName(),
        interest.getKeywords(),
        interest.getSubscriptionsCount(),
        true);  //일단 트루 처리
  }

  @Override
  public void removeInterest(UUID interestId) {
    Interest interest = interestRepository.findById(interestId)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 관심사입니다."));

    // ✅ Setter나 Builder 없이 물리 삭제
    interestRepository.delete(interest);
  }
}
