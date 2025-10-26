package com.spring.monew.notification.controller;

import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.controller.dto.response.NotificationMapper;
import com.spring.monew.notification.domain.Notification;
import com.spring.monew.notification.repository.NotificationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationRepository repository;

  public NotificationController(NotificationRepository repository) {
    this.repository = repository;
  }

  // ===== 목록 조회 =====
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
    var afterOrNow = (after == null) ? Instant.now() : after;

    CursorDecoded decoded;
    try {
      decoded = decodeCursor(cursor);
    } catch (IllegalArgumentException | DateTimeParseException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 커서 형식입니다.", e);
    }
    var cursorAt = decoded == null ? null : decoded.createdAt;
    var cursorId = decoded == null ? null : decoded.id;

    // limit+1로 다음 페이지 여부
    int fetchSize = limit + 1;

    List<Notification> entities = repository.findUnreadByUserIdWithCursor(
        userId, afterOrNow, cursorAt, cursorId, fetchSize);

    boolean hasNext = entities.size() == fetchSize;
    if (hasNext) entities = entities.subList(0, fetchSize - 1);

    var content = entities.stream()
        .map(NotificationMapper::toDto)
        .toList();

    String nextCursor = null;
    if (hasNext && !entities.isEmpty()) {
      var last = entities.get(entities.size() - 1);
      nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
    }

    return new CursorPageResponseNotificationDto(
        content,
        nextCursor,
        afterOrNow,
        content.size(),
        null,
        hasNext
    );
  }

  // ===== 단건 확인 =====
  @Operation(summary = "알림 확인(단건)")
  @Transactional
  @PatchMapping("/{notificationId}")
  public NotificationConfirmResponseDto confirmOne(
      @RequestHeader("Monew-Request-User-ID") UUID userId,
      @PathVariable UUID notificationId
  ) {
    var entity = repository.findByIdAndUserId(notificationId, userId)
        .orElseThrow(() -> new IllegalArgumentException("알림을 찾을 수 없습니다."));

    boolean already = entity.isConfirmed();
    if (!already) {
      entity.confirm(); // @PreUpdate updatedAt 세팅
    }
    // JPA 전에 응답해야 해서 표시용
    return new NotificationConfirmResponseDto(
        entity.getId().toString(),
        true,
        already,
        userId.toString(),
        Instant.now()
    );
  }

  // ===== 전체 확인 =====
  @Operation(summary = "전체 알림 확인(일괄)")
  @Transactional
  @PatchMapping
  public BulkConfirmResultDto confirmAll(
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    long updated = repository.confirmAllByUserId(userId);
    long remain = repository.countUnread(userId);
    boolean allConfirmed = (remain == 0L);
    return new BulkConfirmResultDto(
        updated,
        allConfirmed,
        userId.toString(),
        Instant.now()
    );
  }

  // ====== 내부 ======
  private record CursorDecoded(Instant createdAt, UUID id) {}

  private static String encodeCursor(Instant createdAt, UUID id) {
    var raw = createdAt.toString() + "|" + id;
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private static CursorDecoded decodeCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    var raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
    var parts = raw.split("\\|");
    if (parts.length != 2) throw new IllegalArgumentException("Invalid cursor");
    return new CursorDecoded(Instant.parse(parts[0]), UUID.fromString(parts[1]));
  }
}
