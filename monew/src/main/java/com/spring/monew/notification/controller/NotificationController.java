package com.spring.monew.notification.controller;

import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  // 알림 목록 조회 (커서 기반)
  @Operation(summary = "알림 목록 조회")
  @GetMapping
  public CursorPageResponseNotificationDto list(
      @RequestHeader("Monew-Request-User-ID")
      @Parameter(description = "요청 사용자 ID") UUID userId,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit
  ) {
    return notificationService.list(userId, cursor, after, limit);
  }

  // 알림 확인(단건)
  @Operation(summary = "알림 확인(단건)")
  @PatchMapping("/{notificationId}")
  public NotificationConfirmResponseDto confirmOne(
      @RequestHeader("Monew-Request-User-ID") UUID userId,
      @PathVariable UUID notificationId
  ) {
    return notificationService.confirmOne(userId, notificationId);
  }

  // 전체 알림 확인(일괄)
  @Operation(summary = "전체 알림 확인(일괄)")
  @PatchMapping
  public BulkConfirmResultDto confirmAll(
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    return notificationService.confirmAll(userId);
  }
}