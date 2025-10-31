package com.spring.monew.activity.controller;

import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.common.util.RequestUserExtractor;
import io.swagger.v3.oas.annotations.Hidden;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@Validated
public class UserActivityController {

  private final UserActivityService userActivityService;
  private final RequestUserExtractor userExtractor;

  // 1) 내 활동내역 (헤더 없으면 401)
  @Hidden
  @GetMapping("/api/user-activities/me")
  public ResponseEntity<UserActivityDto> myActivity(Principal principal) {
    UUID userId = userExtractor.extractUserId(principal);
    if (userId == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
    return ResponseEntity.ok(userActivityService.getUserActivity(userId));
  }

  // 2) 특정 사용자 활동내역 (헤더 없어도 허용, 있으면 불일치 시 403 선택 적용)
  @GetMapping("/api/user-activities/{userId}")
  public ResponseEntity<UserActivityDto> userActivityById(
      @PathVariable UUID userId,
      Principal principal
  ) {
    UUID reqUserId = userExtractor.extractUserId(principal);
    return ResponseEntity.ok(userActivityService.getUserActivity(userId));
  }
}