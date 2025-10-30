package com.spring.monew.activity.controller;

import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.auth.config.HeaderUserAuthentication;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
//import com.spring.monew.common.util.RequestUserExtractor; //유한님 예시 적용 예정
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/user-activities")
public class UserActivityController {

  private static final Logger log = LoggerFactory.getLogger(UserActivityController.class);

  private final UserActivityService userActivityService;
  //private final RequestUserExtractor userExtractor; //머지 후 적용 예정

  // Principal에서 사용자 UUID 추출
  private UUID extractUserId(Principal principal) {
    if (principal == null) return null;

    // 커스텀 Authentication 타입 지원
    if (principal instanceof HeaderUserAuthentication auth) {
      Object p = auth.getPrincipal();
      if (p instanceof String s && !s.isBlank()) {
        try { return UUID.fromString(s.trim()); } catch (IllegalArgumentException ignored) {}
      }
    }

    // 일반 Principal.name 에 UUID가 들어오는 경우까지 커버
    String name = principal.getName();
    if (name != null && !name.isBlank()) {
      try { return UUID.fromString(name.trim()); } catch (IllegalArgumentException ignored) {}
    }

    return null;
  }

  // 본인 활동 내역 편의 엔드포인트
  @GetMapping(value = "/me", produces = "application/json")
  public ResponseEntity<UserActivityDto> getMine(
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
      @RequestParam(required = false) String cursor,
      Principal principal
  ) {
    //UUID reqUserId = userExtractor.extractUserId(principal); //머지 후 교체
    UUID reqUserId = extractUserId(principal);
    if (reqUserId == null) {
      //log.warn("401 userId null: principal={}", principal);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
    return ResponseEntity.ok(userActivityService.getUserActivity(reqUserId, limit, cursor));
  }

  // 특정 userId의 활동 내역: 본인만 허용
  @GetMapping(value = "/{userId}", produces = "application/json")
  public ResponseEntity<UserActivityDto> getByUserId(
      @PathVariable UUID userId,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
      @RequestParam(required = false) String cursor,
      Principal principal
  ) {
    //UUID reqUserId = userExtractor.extractUserId(principal); // 머지 후 교체 예정
    UUID reqUserId = extractUserId(principal);
    if (reqUserId == null) {
      //log.warn("401 userId null: pathUserId={}", userId);
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
    if (!userId.equals(reqUserId)) {
      //log.warn("403 mismatch: pathUserId={}, reqUserId={}", userId, reqUserId);
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "다른 사용자의 활동 내역에는 접근할 수 없습니다.");
    }
    return ResponseEntity.ok(userActivityService.getUserActivity(userId, limit, cursor));
  }
}