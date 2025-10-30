package com.spring.monew.notification.controller;

import com.spring.monew.auth.config.HeaderUserAuthentication;
import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@Validated
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  private UUID extractUserId(Principal principal) {
    if (principal instanceof HeaderUserAuthentication auth) {
      try {
        String s = (String) auth.getPrincipal();
        if (s != null && !s.isBlank()) return UUID.fromString(s);
      } catch (Exception ignored) {}
    }
    return null;
  }

  // 목록: 미확인만, 최신순 커서
  @GetMapping
  public CursorPageResponseNotificationDto list(
      Principal principal,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) Instant after,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
  ) {
    UUID userId = extractUserId(principal);
    if (userId == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
    return notificationService.list(userId, cursor, after, limit);
  }

  // ✅ 단건 확인: PATCH /api/notifications/{notificationId}
  @PatchMapping("/{notificationId}")
  @Operation(summary = "알림 확인")
  public NotificationConfirmResponseDto confirmOne(
      Principal principal,
      @PathVariable UUID notificationId
  ) {
    UUID userId = extractUserId(principal);
    if (userId == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
    return notificationService.confirmOne(userId, notificationId);
  }

  // ✅ 전체 확인: PATCH /api/notifications
  @PatchMapping
  @Operation(summary = "전체 알림 확인")
  public BulkConfirmResultDto confirmAll(Principal principal) {
    UUID userId = extractUserId(principal);
    if (userId == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
    return notificationService.confirmAll(userId);
  }
}