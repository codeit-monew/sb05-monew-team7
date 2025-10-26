package com.spring.monew.interest.service.impl;

import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.interest.service.InterestService;
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
    if(interestRepository.existsByName(registerRequest.name())){
      throw new IllegalArgumentException("같은 이름 존재");
    }
    Interest save = interestRepository.save(new Interest(registerRequest.name(), registerRequest.keywords()));
    return new InterestDto(
        save.getId(),
        save.getName(),
        save.getKeywords(),
        save.getSubscriptionsCount(),
        true);  //일단 트루 처리
  }

  @Override
  public CursorPageResponseInterestDto getInterests() {
    return null;
  }

  @Override
  public InterestDto modifyInterest() {
    return null;
  }

  @Override
  public void removeInterest() {

  }
}
