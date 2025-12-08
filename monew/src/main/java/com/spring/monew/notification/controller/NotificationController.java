package com.spring.monew.notification.controller;

import com.spring.monew.auth.config.HeaderUserAuthentication;
import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Tag(name = "알림 관리", description = "알림 관련 API")
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
  @Operation(
      summary = "알림 목록 조회",
      description = "알림 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
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

  // 단건 확인: PATCH /api/notifications/{notificationId}
  @Operation(
      summary = "알림 확인",
      description = "알림을 확인합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "전체 알림 확인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{notificationId}")
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

  // 전체 확인: PATCH /api/notifications
  @Operation(
      summary = "전체 알림 확인",
      description = "전체 알림을 한번에 확인합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "알림 확인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping
  public BulkConfirmResultDto confirmAll(Principal principal) {
    UUID userId = extractUserId(principal);
    if (userId == null) {
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
    }
    return notificationService.confirmAll(userId);
  }
}