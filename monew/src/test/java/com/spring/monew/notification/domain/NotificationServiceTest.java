package com.spring.monew.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.repository.NotificationRepository;
import com.spring.monew.notification.service.NotificationService;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock NotificationRepository repository;
  @InjectMocks NotificationService service;

  UUID userId;
  Instant fixedNow;

  @BeforeEach
  void before() {
    userId = UUID.randomUUID();
    fixedNow = Instant.parse("2025-10-23T04:22:14.468Z");
    // 일부 테스트에서 now가 사용되지 않으므로 불필요 스텁 경고 방지용 lenient
    lenient().when(repository.getDatabaseNow()).thenReturn(fixedNow);
  }

  // ===== 목록(list) =====

  @Test
  @DisplayName("list: 최초 페이지(커서 없음) 정상 조회 - hasNext=false, nextCursor=null")
  void list_firstPage_ok() {
    int limit = 2; // safeLimit = 2 → fetchSize = 3
    var n1 = notif(userId, "A", NotificationResourceType.ARTICLE, fixedNow.minusSeconds(10));
    var n2 = notif(userId, "B", NotificationResourceType.COMMENT, fixedNow.minusSeconds(20));

    when(repository.findUnreadByUserIdWithCursor(eq(userId), any(), isNull(), isNull(), eq(limit + 1)))
        .thenReturn(List.of(n1, n2));
    when(repository.countUnreadByUserId(userId)).thenReturn(2L);

    CursorPageResponseNotificationDto dto = service.list(userId, null, null, limit);

    assertThat(dto.content()).hasSize(2);
    assertThat(dto.hasNext()).isFalse();
    assertThat(dto.nextCursor()).isNull();
    assertThat(dto.size()).isEqualTo(2);
    assertThat(dto.totalElements()).isEqualTo(2);
    assertThat(dto.nextAfter()).isEqualTo(fixedNow);
  }

  @Test
  @DisplayName("list: hasNext=true 이면 nextCursor 생성되고 content는 limit개로 잘린다")
  void list_hasNext_true_generates_nextCursor() {
    int limit = 1; // fetchSize = 2
    var newer = notif(userId, "NEW", NotificationResourceType.ARTICLE, fixedNow.minusSeconds(1));
    var older = notif(userId, "OLD", NotificationResourceType.COMMENT, fixedNow.minusSeconds(2));

    when(repository.findUnreadByUserIdWithCursor(eq(userId), any(), isNull(), isNull(), eq(limit + 1)))
        .thenReturn(List.of(newer, older));
    when(repository.countUnreadByUserId(userId)).thenReturn(2L);

    CursorPageResponseNotificationDto dto = service.list(userId, null, null, limit);

    assertThat(dto.content()).hasSize(1);
    assertThat(dto.hasNext()).isTrue();
    assertThat(dto.nextCursor()).isNotBlank();
    assertThat(dto.size()).isEqualTo(1);
  }

  @Test
  @DisplayName("list: 잘못된 커서면 400(BAD_REQUEST)")
  void list_invalid_cursor_400() {
    String badCursor = "THIS_IS_NOT_BASE64";
    assertThatThrownBy(() -> service.list(userId, badCursor, null, 20))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("400");
  }

  @Test
  @DisplayName("list: after가 DB now보다 미래면 now로 보정")
  void list_after_future_is_corrected_to_now() {
    Instant future = fixedNow.plusSeconds(300);
    ArgumentCaptor<Instant> upper = ArgumentCaptor.forClass(Instant.class);

    when(repository.findUnreadByUserIdWithCursor(eq(userId), any(), any(), any(), anyInt()))
        .thenReturn(List.of());
    when(repository.countUnreadByUserId(userId)).thenReturn(0L);

    service.list(userId, null, future, 20);

    verify(repository).findUnreadByUserIdWithCursor(eq(userId), upper.capture(), any(), any(), anyInt());
    assertThat(upper.getValue()).isEqualTo(fixedNow);
  }

  @Test
  @DisplayName("list: limit 하한/상한 보정(1~100) 후 fetchSize = safeLimit+1")
  void list_limit_bounds() {
    // 너무 작은 값(0) → safeLimit=1 → fetchSize=2
    when(repository.findUnreadByUserIdWithCursor(eq(userId), any(), any(), any(), eq(2)))
        .thenReturn(List.of());
    when(repository.countUnreadByUserId(userId)).thenReturn(0L);
    service.list(userId, null, null, 0);
    verify(repository).findUnreadByUserIdWithCursor(eq(userId), any(), any(), any(), eq(2));

    // 너무 큰 값(1000) → safeLimit=100 → fetchSize=101
    reset(repository);
    // reset 이후 now 재스텁 (lenient)
    lenient().when(repository.getDatabaseNow()).thenReturn(fixedNow);
    when(repository.findUnreadByUserIdWithCursor(eq(userId), any(), any(), any(), eq(101)))
        .thenReturn(List.of());
    when(repository.countUnreadByUserId(userId)).thenReturn(0L);

    service.list(userId, null, null, 1000);

    verify(repository).findUnreadByUserIdWithCursor(eq(userId), any(), any(), any(), eq(101));
  }

  // ===== confirmOne =====

  @Test
  @DisplayName("confirmOne: 대상 없음 → 404")
  void confirmOne_notFound_404() {
    UUID nId = UUID.randomUUID();
    when(repository.findByIdAndUserId(nId, userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.confirmOne(userId, nId))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("404");
  }

  @Test
  @DisplayName("confirmOne: 이미 확인된 알림 → alreadyConfirmed=true, flush 호출 없음")
  void confirmOne_alreadyConfirmed_true() {
    UUID nId = UUID.randomUUID();
    var n = notif(userId, "X", NotificationResourceType.ARTICLE, fixedNow.minusSeconds(10));
    setId(n, nId);
    setConfirmed(n, true);

    when(repository.findByIdAndUserId(nId, userId)).thenReturn(Optional.of(n));

    NotificationConfirmResponseDto resp = service.confirmOne(userId, nId);

    assertThat(resp.id()).isEqualTo(nId);
    assertThat(resp.confirmed()).isTrue();
    assertThat(resp.alreadyConfirmed()).isTrue();
    assertThat(resp.userId()).isEqualTo(userId);
    assertThat(resp.updatedAt()).isNotNull();
    verify(repository, never()).flush();
  }

  @Test
  @DisplayName("confirmOne: 미확인 → 확인 처리, flush 호출, alreadyConfirmed=false")
  void confirmOne_to_confirmed_calls_flush() {
    UUID nId = UUID.randomUUID();
    var n = notif(userId, "Y", NotificationResourceType.COMMENT, fixedNow.minusSeconds(20));
    setId(n, nId);
    setConfirmed(n, false);

    when(repository.findByIdAndUserId(nId, userId)).thenReturn(Optional.of(n));

    NotificationConfirmResponseDto resp = service.confirmOne(userId, nId);

    assertThat(resp.id()).isEqualTo(nId);
    assertThat(resp.confirmed()).isTrue();
    assertThat(resp.alreadyConfirmed()).isFalse();
    assertThat(resp.userId()).isEqualTo(userId);
    assertThat(resp.updatedAt()).isNotNull();
    verify(repository).flush();
  }

  // ===== confirmAll =====

  @Test
  @DisplayName("confirmAll: 업데이트 건수/상태/시각 검증")
  void confirmAll_ok() {
    when(repository.confirmAllByUserId(userId)).thenReturn(2L);
    when(repository.existsByUserIdAndConfirmedFalse(userId)).thenReturn(false);

    BulkConfirmResultDto dto = service.confirmAll(userId);

    assertThat(dto.updatedCount()).isEqualTo(2L);
    assertThat(dto.allConfirmed()).isTrue();
    assertThat(dto.userId()).isEqualTo(userId);
    assertThat(dto.processedAt()).isEqualTo(fixedNow);
  }

  @Test
  @DisplayName("confirmAll: 여전히 미확인 남으면 allConfirmed=false")
  void confirmAll_still_unread_false() {
    when(repository.confirmAllByUserId(userId)).thenReturn(5L);
    when(repository.existsByUserIdAndConfirmedFalse(userId)).thenReturn(true);

    BulkConfirmResultDto dto = service.confirmAll(userId);

    assertThat(dto.updatedCount()).isEqualTo(5L);
    assertThat(dto.allConfirmed()).isFalse();
    assertThat(dto.processedAt()).isEqualTo(fixedNow);
  }

  // ===== 헬퍼 =====

  private static Notification notif(UUID userId, String content, NotificationResourceType type, Instant createdAt) {
    Notification n = Notification.of(userId, content, type, UUID.randomUUID());
    setField(n, "createdAt", createdAt);
    setField(n, "updatedAt", createdAt);
    return n;
  }

  private static void setId(Notification n, UUID id) {
    setField(n, "id", id);
  }

  private static void setConfirmed(Notification n, boolean val) {
    setField(n, "confirmed", val);
    if (n.getCreatedAt() == null) {
      setField(n, "createdAt", Instant.now());
      setField(n, "updatedAt", n.getCreatedAt());
    }
  }

  private static void setField(Object target, String name, Object value) {
    try {
      Field f = target.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(target, value);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
