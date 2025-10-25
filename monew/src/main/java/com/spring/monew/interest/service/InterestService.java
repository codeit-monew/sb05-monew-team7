package com.spring.monew.interest.service;

import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;

public interface InterestService {

  InterestDto addInterest();

  CursorPageResponseInterestDto getInterests();

  InterestDto modifyInterest();

  void removeInterest();
}
