package com.spring.monew.notification.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.auth.config.HeaderUserAuthentication;
import com.spring.monew.config.TestSecurityConfig;
import com.spring.monew.notification.controller.dto.response.CursorPageResponseNotificationDto;
import com.spring.monew.notification.controller.dto.response.NotificationConfirmResponseDto;
import com.spring.monew.notification.controller.dto.response.NotificationDto;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc // 보안 필터 활성화
class NotificationControllerTest {

  @Autowired
  MockMvc mvc;
  @Autowired
  ObjectMapper mapper;

  @MockitoBean
  NotificationService notificationService;
  // ⚠️ RequestUserExtractor는 사용되지 않으므로 목/스터빙/검증을 두지 않습니다.

  final UUID userId = UUID.randomUUID();
  final UUID notificationId = UUID.randomUUID();

  @Test
  @DisplayName("알림 목록 조회 성공 (커서 페이지)")
  void notificationList_success() throws Exception {
    String cursor = "encoded-cursor";
    Instant after = Instant.parse("2025-11-01T09:00:00Z");
    int limit = 50;

    NotificationDto row = new NotificationDto(
        UUID.randomUUID(),
        Instant.parse("2025-11-01T09:00:00Z"),
        Instant.parse("2025-11-01T09:00:00Z"),
        false,
        userId,
        "[알림] 새 기사 알림입니다.",
        NotificationResourceType.ARTICLE,
        UUID.randomUUID()
    );
    CursorPageResponseNotificationDto page =
        new CursorPageResponseNotificationDto(List.of(row), cursor, after, 1, 0L, true);

    when(notificationService.list(eq(userId), eq(cursor), eq(after), eq(limit))).thenReturn(page);

    mvc.perform(get("/api/notifications")
            // 실제 인증 컨텍스트 채움 (메서드 보안/필터 통과)
            .with(authentication(new HeaderUserAuthentication(userId.toString())))
            // 일부 구현은 헤더도 참고하므로 함께 제공(안전장치)
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
    Instant now = Instant.parse("2025-11-01T10:00:00Z");
    NotificationConfirmResponseDto res =
        new NotificationConfirmResponseDto(notificationId, true, false, userId, now);

    when(notificationService.confirmOne(eq(userId), eq(notificationId))).thenReturn(res);

    mvc.perform(patch("/api/notifications/{notificationId}", notificationId)
            .with(authentication(new HeaderUserAuthentication(userId.toString())))
            .header("Monew-Request-User-ID", userId.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(notificationId.toString())))
        .andExpect(jsonPath("$.confirmed", is(true)))
        .andExpect(jsonPath("$.alreadyConfirmed", is(false)))
        .andExpect(jsonPath("$.userId", is(userId.toString())));

    verify(notificationService).confirmOne(eq(userId), eq(notificationId));
  }
}