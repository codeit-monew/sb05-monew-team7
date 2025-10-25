package com.spring.monew.interest.service.impl;

import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
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
  public InterestDto addInterest() {
    return null;
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
