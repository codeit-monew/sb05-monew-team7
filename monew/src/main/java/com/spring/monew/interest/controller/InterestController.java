package com.spring.monew.interest.controller;

import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.service.InterestService;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

  private final InterestService interestService;
  private final RequestUserExtractor userExtractor;

  @PostMapping
  public InterestDto interestAdd(@RequestBody InterestRegisterRequest registerRequest) {
    return interestService.addInterest(registerRequest);
  }

  @GetMapping
  public CursorPageResponseInterestDto interestList(
      @RequestParam(required = false) String keyword,               // 검색어 (nullable)
      @RequestParam(defaultValue = "name") String orderBy,          // 정렬 기준 (기본값 name)
      @RequestParam(defaultValue = "ASC") String direction,         // 정렬 방향 (기본값 ASC)
      @RequestParam(required = false) String cursor,                // 커서 값
      @RequestParam(required = false) Instant after,                // 보조 커서(createdAt)
      @RequestParam(defaultValue = "50") int limit,                 // 페이지 크기
      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);
    return interestService.getInterests(keyword, orderBy, direction, cursor, after, limit, userId);
  }

  @PatchMapping("/{interestId}")
  public InterestDto interestModify(@PathVariable UUID interestId,
      @RequestBody InterestUpdateRequest updateRequest) {
    return interestService.modifyInterest(interestId, updateRequest);
  }

  @DeleteMapping("/{interestId}")
  public void interestRemove(@PathVariable UUID interestId) {
    interestService.removeInterest(interestId);
  }
}
