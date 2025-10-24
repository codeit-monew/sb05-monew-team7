package com.spring.monew.interest.controller;

import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.service.InterestService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

  private final InterestService interestService;

  @PostMapping
  public InterestDto interestAdd(@RequestBody InterestRegisterRequest registerRequest) {
    return interestService.addInterest(registerRequest);
  }

  @GetMapping
  public CursorPageResponseInterestDto interestList() {
    return null;
  }

  @PatchMapping("/{interestId}")
  public InterestDto interestModify(@PathVariable UUID interestId) {
    return null;
  }

  @DeleteMapping("/{interestId}")
  public void interestRemove(@PathVariable UUID interestId) {

  }
}
