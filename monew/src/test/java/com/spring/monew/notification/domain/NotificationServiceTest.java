package com.spring.monew.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.controller.dto.response.NotificationDto;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.repository.NotificationRepository;
import com.spring.monew.notification.service.NotificationService;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 실제 구현 클래스(com.spring.monew.notification.service.NotificationService)에
 * @InjectMocks로 주입하고, Repository만 목킹합니다.
 * - list(UUID, String, Instant, int)
 * - confirmOne(UUID, UUID)
 * - confirmAll(UUID)
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

  @Mock private NotificationRepository repository;

  @InjectMocks private NotificationService service; // 구현 클래스 이름이 Service임(Impl 아님)

  UUID userId;
  UUID notificationId;

  @BeforeEach
  void setup() {
    userId = UUID.randomUUID();
    notificationId = UUID.randomUUID();
  }

  @Test
  @DisplayName("알림 목록 조회 성공 - 빈 결과(커서/카운트/hasNext 계산)")
  void list_empty_success() {
    String cursor = null; // 커서 없이 최초 페이지
    Instant now = Instant.parse("2025-11-01T09:00:00Z");
    Instant after = now;  // after가 null이 아니고 now보다 미래가 아니도록
    int limit = 50;

    when(repository.getDatabaseNow()).thenReturn(now);
    when(repository.findUnreadByUserIdWithCursor(
        eq(userId), eq(after), eq(null), eq(null), anyInt()))
        .thenReturn(Collections.emptyList());
    when(repository.countUnreadByUserId(eq(userId))).thenReturn(0L);

    CursorPageResponseNotificationDto res = service.list(userId, cursor, after, limit);

    assertThat(res).isNotNull();
    assertThat(res.content()).isEmpty();
    assertThat(res.size()).isEqualTo(0);
    assertThat(res.totalElements()).isEqualTo(0L);
    assertThat(res.hasNext()).isFalse();
    assertThat(res.nextAfter()).isEqualTo(after);

    verify(repository).getDatabaseNow();
    verify(repository).findUnreadByUserIdWithCursor(
        eq(userId), eq(after), eq(null), eq(null), anyInt());
    verify(repository).countUnreadByUserId(eq(userId));
  }

  @Test
  @DisplayName("알림 단건 확인 성공 - real entity 사용(최소 목킹)")
  void confirmOne_success() {
    // given
    Instant created = Instant.parse("2025-11-01T10:00:00Z");

    // 실제 엔티티 생성
    UUID rid = UUID.randomUUID();
    Notification entity = Notification.of(userId, "to-confirm", NotificationResourceType.ARTICLE, rid);

    // 생성/수정 시각과 confirmed 초기값을 통제
    ReflectionTestUtils.setField(entity, "createdAt", created);
    ReflectionTestUtils.setField(entity, "updatedAt", created);
    ReflectionTestUtils.setField(entity, "confirmed", false);

    when(repository.findByIdAndUserId(eq(notificationId), eq(userId)))
        .thenReturn(Optional.of(entity));

    // when
    NotificationConfirmResponseDto res = service.confirmOne(userId, notificationId);

    // then
    assertThat(res).isNotNull();
    assertThat(res.id()).isEqualTo(entity.getId());     // 서비스가 entity id를 그대로 반환
    assertThat(res.userId()).isEqualTo(userId);
    assertThat(res.confirmed()).isTrue();
    // confirm()으로 인해 updatedAt 이 갱신되었을 수도 있으니 null 아님만 체크
    assertThat(res.updatedAt()).isNotNull();

    verify(repository).findByIdAndUserId(eq(notificationId), eq(userId));
    verify(repository).flush();
  }

  @Test
  @DisplayName("알림 전체 확인 성공 - confirmAllByUserId/existsByUserIdAndConfirmedFalse/getDatabaseNow")
  void confirmAll_success() {
    Instant dbNow = Instant.parse("2025-11-01T10:30:00Z");

    when(repository.confirmAllByUserId(eq(userId))).thenReturn(12L);
    when(repository.existsByUserIdAndConfirmedFalse(eq(userId))).thenReturn(false);
    when(repository.getDatabaseNow()).thenReturn(dbNow);

    BulkConfirmResultDto res = service.confirmAll(userId);

    assertThat(res).isNotNull();
    assertThat(res.updatedCount()).isEqualTo(12L);
    assertThat(res.allConfirmed()).isTrue();
    assertThat(res.userId()).isEqualTo(userId);
    assertThat(res.processedAt()).isEqualTo(dbNow);

    verify(repository).confirmAllByUserId(eq(userId));
    verify(repository).existsByUserIdAndConfirmedFalse(eq(userId));
    verify(repository).getDatabaseNow();
  }

  @Test
  @DisplayName("알림 목록 조회 매핑 확인 - 엔티티 1건 → DTO 필드")
  void list_mapping_single() {
    Instant now = Instant.parse("2025-11-01T12:00:00Z");
    Instant after = now;
    int limit = 20;

    // 엔티티 1건 모킹
    UUID rid = UUID.randomUUID();
    Notification entity = mock(Notification.class);
    when(entity.getId()).thenReturn(UUID.randomUUID());
    when(entity.getCreatedAt()).thenReturn(now.minusSeconds(5));
    when(entity.getUpdatedAt()).thenReturn(now.minusSeconds(3));
    when(entity.isConfirmed()).thenReturn(false);
    when(entity.getUserId()).thenReturn(userId);
    when(entity.getResourceType()).thenReturn(NotificationResourceType.ARTICLE);
    when(entity.getResourceId()).thenReturn(rid);
    when(entity.getContent()).thenReturn("테스트 알림");

    when(repository.getDatabaseNow()).thenReturn(now);
    when(repository.findUnreadByUserIdWithCursor(eq(userId), eq(after), eq(null), eq(null), anyInt()))
        .thenReturn(List.of(entity));
    when(repository.countUnreadByUserId(eq(userId))).thenReturn(1L);

    CursorPageResponseNotificationDto res = service.list(userId, null, after, limit);

    assertThat(res).isNotNull();
    assertThat(res.content()).hasSize(1);
    NotificationDto dto = res.content().get(0);
    assertThat(dto.userId()).isEqualTo(userId);
    assertThat(dto.content()).isEqualTo("테스트 알림");
    assertThat(dto.resourceType()).isEqualTo(NotificationResourceType.ARTICLE);
    assertThat(dto.resourceId()).isEqualTo(rid);

    verify(repository).findUnreadByUserIdWithCursor(eq(userId), eq(after), eq(null), eq(null), anyInt());
  }
}
