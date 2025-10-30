package com.spring.monew.activity.controller;

import com.spring.monew.activity.controller.dto.response.UserActivityResponse;
import com.spring.monew.activity.repository.UserActivityQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Validated //@Min/@Max 등 메서드 파라미터 검증
public class UserActivityController {

  private final UserActivityQueryService service;

  @GetMapping(value = "/api/user-activities/{userId}", produces = "application/json")
  public ResponseEntity<UserActivityResponse> get(
      @PathVariable UUID userId,
      @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID reqUserId,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit,
      @RequestParam(required = false) String cursor
  ) {
    return ResponseEntity.ok(service.getUserActivity(userId, limit, cursor));
  }
}
