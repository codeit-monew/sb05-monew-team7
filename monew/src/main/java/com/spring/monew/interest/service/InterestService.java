package com.spring.monew.interest.service;

import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;

public interface InterestService {

  InterestDto addInterest(InterestRegisterRequest registerRequest);

  CursorPageResponseInterestDto getInterests();

  InterestDto modifyInterest();

  void removeInterest();
}
