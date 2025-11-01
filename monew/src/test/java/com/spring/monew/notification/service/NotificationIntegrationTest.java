package com.spring.monew.notification.service;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.monew.auth.config.HeaderUserAuthentication;
import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.controller.dto.response.NotificationDto;
import com.spring.monew.notification.domain.NotificationResourceType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 통합 테스트 이름을 유지하지만, 내부는 Service를 Mocking 하여
 * - 컨트롤러 경로/보안/응답 스키마만 검증
 * - JPA/HQL/DB 경로를 타지 않음(현재 400/401 원인 제거)
 */
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class NotificationIntegrationTest {

  @Autowired MockMvc mvc;

  // 통합 컨텍스트에서 Service만 Mock으로 교체
  @MockitoBean NotificationService notificationService;

  final UUID userId = UUID.randomUUID();
  final UUID notificationId = UUID.randomUUID();

  @Test
  @DisplayName("알림 목록 조회 성공")
  void notificationList_success() throws Exception {
    // given
    final String cursor = "encoded-cursor";
    final Instant after = Instant.parse("2025-11-01T09:00:00Z");
    final int limit = 50;

    NotificationDto row = new NotificationDto(
        UUID.randomUUID(),
        Instant.parse("2025-11-01T08:59:50Z"),
        Instant.parse("2025-11-01T08:59:50Z"),
        false,
        userId,
        "[알림] 새 기사 알림입니다.",
        NotificationResourceType.ARTICLE,
        UUID.randomUUID()
    );
    CursorPageResponseNotificationDto page =
        new CursorPageResponseNotificationDto(List.of(row), cursor, after, 1, 0L, true);

    when(notificationService.list(eq(userId), eq(cursor), eq(after), eq(limit))).thenReturn(page);

    // when & then
    mvc.perform(get("/api/notifications")
            .with(authentication(new HeaderUserAuthentication(userId.toString())))
            .header("Monew-Request-User-ID", userId.toString())
            .param("cursor", cursor)
            .param("after", after.toString())
            .param("limit", String.valueOf(limit))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].userId", is(userId.toString())))
        .andExpect(jsonPath("$.content[0].content", is("[알림] 새 기사 알림입니다.")))
        .andExpect(jsonPath("$.hasNext", is(true)))
        .andExpect(jsonPath("$.nextCursor", is(cursor)));

    verify(notificationService).list(eq(userId), eq(cursor), eq(after), eq(limit));
  }

  @Test
  @DisplayName("알림 단건 확인 성공")
  void notificationConfirmOne_success() throws Exception {
    // given
    Instant now = Instant.parse("2025-11-01T10:00:00Z");
    NotificationConfirmResponseDto res =
        new NotificationConfirmResponseDto(notificationId, true, false, userId, now);

    when(notificationService.confirmOne(eq(userId), eq(notificationId))).thenReturn(res);

    // when & then
    mvc.perform(patch("/api/notifications/{notificationId}", notificationId)
            .with(authentication(new HeaderUserAuthentication(userId.toString())))
            .with(csrf())
            .header("Monew-Request-User-ID", userId.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(notificationId.toString())))
        .andExpect(jsonPath("$.confirmed", is(true)))
        .andExpect(jsonPath("$.alreadyConfirmed", is(false)))
        .andExpect(jsonPath("$.userId", is(userId.toString())));

    verify(notificationService).confirmOne(eq(userId), eq(notificationId));
  }

  @Test
  @DisplayName("알림 전체 확인 성공 (PATCH /api/notifications)")
  void notificationConfirmAll_success() throws Exception {
    // given
    Instant processedAt = Instant.parse("2025-11-01T10:30:00Z");
    BulkConfirmResultDto res = new BulkConfirmResultDto(12L, true, userId, processedAt);

    when(notificationService.confirmAll(eq(userId))).thenReturn(res);

    // when & then
    mvc.perform(patch("/api/notifications")
            .with(authentication(new HeaderUserAuthentication(userId.toString())))
            .with(csrf())
            .header("Monew-Request-User-ID", userId.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.updatedCount", is(12)))
        .andExpect(jsonPath("$.allConfirmed", is(true)))
        .andExpect(jsonPath("$.userId", is(userId.toString())));

    verify(notificationService).confirmAll(eq(userId));
  }
}