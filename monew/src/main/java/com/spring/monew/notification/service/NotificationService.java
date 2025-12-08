package com.spring.monew.notification.service;

import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.controller.dto.response.NotificationDto;
import com.spring.monew.notification.domain.Notification;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.repository.NotificationRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final int MIN_LIMIT = 1;
  private static final int MAX_LIMIT = 100;

  private final NotificationRepository repository;

  // ===== 목록 조회 (커서 기반) =====
  @Transactional(readOnly = true)
  public CursorPageResponseNotificationDto list(UUID userId, String cursor, Instant after,
      int limit) {
    Objects.requireNonNull(userId, "userId must not be null");

    final int safeLimit = Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, limit));

    // DB now() 우선, after가 미래면 now로 보정
    final Instant now = getConsistentNow();
    final Instant afterOrNow = (after == null || after.isAfter(now)) ? now : after;

    final CursorDecoded decoded;
    try {
      decoded = decodeCursor(cursor);
    } catch (IllegalArgumentException | DateTimeParseException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 커서 형식입니다.", e);
    }

    final Instant cursorAt = (decoded == null) ? null : decoded.createdAt;
    final UUID cursorId = (decoded == null) ? null : decoded.id;

    // 다음 페이지 여부 확인을 위해 +1
    final int fetchSize = safeLimit + 1;
    List<Notification> entities = repository.findUnreadByUserIdWithCursor(
        userId, afterOrNow, cursorAt, cursorId, fetchSize
    );

    final boolean hasNext = entities.size() == fetchSize;
    if (hasNext) {
      entities = entities.subList(0, fetchSize - 1);
    }

    final List<NotificationDto> content = entities.stream()
        .map(n -> new NotificationDto(
            n.getId(),
            n.getCreatedAt(),
            n.getUpdatedAt(),   // 없다면 null 유지
            n.isConfirmed(),
            n.getUserId(),
            n.getContent(),
            n.getResourceType(),
            n.getResourceId()
        ))
        .toList();

    String nextCursor = null;
    if (hasNext && !entities.isEmpty()) {
      Notification last = entities.get(entities.size() - 1);
      nextCursor = encodeCursor(last.getCreatedAt(), last.getId());
    }
    long totalUnread = repository.countUnreadByUserId(userId);

    return new CursorPageResponseNotificationDto(
        content,
        nextCursor,
        afterOrNow,
        content.size(),
        totalUnread,
        hasNext
    );
  }

  // ===== 단건 확인 =====
  @Transactional
  public NotificationConfirmResponseDto confirmOne(UUID userId, UUID notificationId) {
    Objects.requireNonNull(userId, "userId must not be null");
    Objects.requireNonNull(notificationId, "notificationId must not be null");

    Notification entity = repository.findByIdAndUserId(notificationId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));

    boolean already = entity.isConfirmed();
    if (!already) {
      entity.confirm();   // @PreUpdate 로 updatedAt 반영
      repository.flush(); // 즉시 반영
    }

    return new NotificationConfirmResponseDto(
        entity.getId(),
        true,
        already,
        userId,
        (entity.getUpdatedAt() != null) ? entity.getUpdatedAt() : entity.getCreatedAt()
    );
  }

  // ===== 전체 확인 =====
  @Transactional
  public BulkConfirmResultDto confirmAll(UUID userId) {
    Objects.requireNonNull(userId, "userId must not be null");

    long updated = repository.confirmAllByUserId(userId);
    boolean hasUnread = repository.existsByUserIdAndConfirmedFalse(userId);

    return new BulkConfirmResultDto(
        updated,
        !hasUnread,
        userId,
        getConsistentNow()
    );
  }

  // ===== 내부 유틸 =====
  private static final class CursorDecoded {

    private final Instant createdAt;
    private final UUID id;

    private CursorDecoded(Instant createdAt, UUID id) {
      this.createdAt = createdAt;
      this.id = id;
    }
  }

  private static String encodeCursor(Instant createdAt, UUID id) {
    String raw = createdAt.toString() + "|" + id;
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private static CursorDecoded decodeCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) {
      return null;
    }
    String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
    String[] parts = raw.split("\\|");
    if (parts.length != 2) {
      throw new IllegalArgumentException("Invalid cursor. expected 'createdAt|id'");
    }
    return new CursorDecoded(Instant.parse(parts[0]), UUID.fromString(parts[1]));
  }

  @Transactional
  public void create(UUID userId, String nickname, NotificationResourceType type, UUID resourceId) {
    String content = nickname + "님이 나의 댓글을 좋아합니다.";
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(content, "content");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(resourceId, "resourceId");

    Notification n = Notification.of(userId, content, type, resourceId);
    repository.save(n);
  }

  @Transactional
  public void createForUsers(Collection<UUID> userIds, String content,
      NotificationResourceType type, UUID resourceId) {
    if (userIds == null || userIds.isEmpty()) {
      return;
    }
    List<Notification> list = userIds.stream()
        .map(uid -> Notification.of(uid, content, type, resourceId))
        .toList();
    repository.saveAll(list);
  }

  // DB 시간 우선 사용 — 예외를 숨기지 않고 그대로 던져 원인 파악 가능
  private Instant getConsistentNow() {
    return repository.getDatabaseNow();
  }
}