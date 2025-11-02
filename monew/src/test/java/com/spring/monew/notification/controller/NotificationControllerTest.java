package com.spring.monew.notification.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.config.TestSecurityConfig;
import com.spring.monew.notification.controller.dto.response.BulkConfirmResultDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class NotificationControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @MockitoBean NotificationService notificationService;
  @MockitoBean RequestUserExtractor userExtractor;

  // ===== 목록 =====
  @Test
  @DisplayName("GET /api/notifications - 정상 조회 (non-empty)")
  void list_ok() throws Exception {
    UUID userId = UUID.randomUUID();
    var page = new CursorPageResponseNotificationDto(
        List.of(
            new NotificationDto(UUID.randomUUID(), Instant.now(), null, false, userId,
                "테스트 알림-기사", NotificationResourceType.ARTICLE, UUID.randomUUID()),
            new NotificationDto(UUID.randomUUID(), Instant.now(), null, false, userId,
                "테스트 알림-댓글", NotificationResourceType.COMMENT, UUID.randomUUID())
        ),
        null, Instant.now(), 2, 2L, false
    );
    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(notificationService.list(eq(userId), any(), any(), anyInt())).willReturn(page);

    mvc.perform(get("/api/notifications")
            .param("limit", "50")
            .header("Monew-Request-User-ID", userId) // 실제 추출은 userExtractor가 처리
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.hasNext").value(false));
  }

  @Test
  @DisplayName("GET /api/notifications - 빈 목록")
  void list_empty() throws Exception {
    UUID userId = UUID.randomUUID();
    var empty = new CursorPageResponseNotificationDto(List.of(), null, null, 0, 0L, false);
    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(notificationService.list(eq(userId), eq(null), eq(null), eq(20))).willReturn(empty);

    mvc.perform(get("/api/notifications")
            .param("limit", "20")
            .header("Monew-Request-User-ID", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty())
        .andExpect(jsonPath("$.hasNext", is(false)))
        .andExpect(jsonPath("$.size").value(0))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  @DisplayName("GET /api/notifications - 잘못된 커서 → 400")
  void list_bad_cursor_400() throws Exception {
    UUID userId = UUID.randomUUID();
    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(notificationService.list(eq(userId), eq("BAD_CURSOR"), any(), anyInt()))
        .willThrow(new org.springframework.web.server.ResponseStatusException(
            HttpStatus.BAD_REQUEST, "잘못된 커서 형식입니다."));

    mvc.perform(get("/api/notifications")
            .param("cursor", "BAD_CURSOR")
            .param("limit", "20")
            .header("Monew-Request-User-ID", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("GET /api/notifications - 인증 헤더/추출 실패 → 401")
  void list_unauthorized() throws Exception {
    given(userExtractor.extractUserId(any())).willReturn(null);

    mvc.perform(get("/api/notifications").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  // ===== 단건 확인 =====
  @Test
  @DisplayName("PATCH /api/notifications/{id} - 이미 확인된 알림")
  void confirm_one_already() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID notiId = UUID.randomUUID();
    var res = new NotificationConfirmResponseDto(notiId, true, true, userId, Instant.now());

    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(notificationService.confirmOne(eq(userId), eq(notiId))).willReturn(res);

    mvc.perform(patch("/api/notifications/{notificationId}", notiId)
            .header("Monew-Request-User-ID", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(notiId.toString()))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.confirmed").value(true))
        .andExpect(jsonPath("$.alreadyConfirmed").value(true))
        .andExpect(jsonPath("$.updatedAt").exists());
  }

  @Test
  @DisplayName("PATCH /api/notifications/{id} - 존재하지 않는 알림 → 404")
  void confirm_one_notfound_404() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID notiId = UUID.randomUUID();

    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(notificationService.confirmOne(eq(userId), eq(notiId)))
        .willThrow(new org.springframework.web.server.ResponseStatusException(
            HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."));

    mvc.perform(patch("/api/notifications/{notificationId}", notiId)
            .header("Monew-Request-User-ID", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  // ===== 전체 확인 =====
  @Test
  @DisplayName("PATCH /api/notifications - 전체 확인 OK")
  void confirm_all_ok() throws Exception {
    UUID userId = UUID.randomUUID();
    var resp = new BulkConfirmResultDto(2L, true, userId, Instant.now());

    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(notificationService.confirmAll(eq(userId))).willReturn(resp);

    mvc.perform(patch("/api/notifications")
            .header("Monew-Request-User-ID", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.updatedCount").value(2))
        .andExpect(jsonPath("$.allConfirmed").value(true))
        .andExpect(jsonPath("$.userId").value(userId.toString()))
        .andExpect(jsonPath("$.processedAt").exists());
  }

  @Test
  @DisplayName("PATCH /api/notifications - 서비스 내부 오류 → 500")
  void confirm_all_500() throws Exception {
    UUID userId = UUID.randomUUID();

    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(notificationService.confirmAll(eq(userId)))
        .willThrow(new org.springframework.web.server.ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, "boom"));

    mvc.perform(patch("/api/notifications")
            .header("Monew-Request-User-ID", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError());
  }
}