package com.spring.monew.activity.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.activity.controller.dto.response.CommentActivityDto;
import com.spring.monew.activity.controller.dto.response.CommentLikeActivityDto;
import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.config.TestSecurityConfig;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
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

@WebMvcTest(UserActivityController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class UserActivityControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  @MockitoBean UserActivityService userActivityService;
  @MockitoBean RequestUserExtractor userExtractor;

  private static UserActivityDto dummy(UUID uid) {
    return new UserActivityDto(
        uid, "u@test.com", "nick", Instant.parse("2025-01-01T00:00:00Z"),
        List.<SubscriptionDto>of(),
        List.<CommentActivityDto>of(),
        List.<CommentLikeActivityDto>of(),
        List.<ArticleViewDto>of()
    );
  }

  @Test
  @DisplayName("GET /api/user-activities/{userId} 공개 조회(헤더 없음) → 200 + 최상위 필드")
  void byUserId_public_ok_without_header() throws Exception {
    UUID uid = UUID.randomUUID();
    when(userExtractor.extractUserId(any())).thenReturn(null); // 헤더 없음
    when(userActivityService.getUserActivity(eq(uid))).thenReturn(dummy(uid));

    mvc.perform(get("/api/user-activities/{userId}", uid)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(uid.toString()))
        .andExpect(jsonPath("$.email").value("u@test.com"))
        .andExpect(jsonPath("$.nickname").value("nick"))
        .andExpect(jsonPath("$.subscriptions").isArray())
        .andExpect(jsonPath("$.comments").isArray())
        .andExpect(jsonPath("$.commentLikes").isArray())
        .andExpect(jsonPath("$.articleViews").isArray());

    verify(userActivityService).getUserActivity(eq(uid));
  }

  @Test
  @DisplayName("GET /api/user-activities/{userId} (소유자 접근) → 200")
  void byUserId_owner_ok() throws Exception {
    UUID uid = UUID.randomUUID();
    when(userExtractor.extractUserId(any())).thenReturn(uid);
    when(userActivityService.getUserActivity(eq(uid))).thenReturn(dummy(uid));

    mvc.perform(get("/api/user-activities/{userId}", uid)
            .header("Monew-Request-User-ID", uid.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(uid.toString()))
        .andExpect(jsonPath("$.email").value("u@test.com"))
        .andExpect(jsonPath("$.nickname").value("nick"));

    verify(userActivityService).getUserActivity(eq(uid));
  }

  @Test
  @DisplayName("GET /api/user-activities/{userId} (타인 접근) → 403")
  void byUserId_forbidden_ifNotOwner() throws Exception {
    UUID owner = UUID.randomUUID();
    UUID other = UUID.randomUUID();
    when(userExtractor.extractUserId(any())).thenReturn(other);

    mvc.perform(get("/api/user-activities/{userId}", owner)
            .header("Monew-Request-User-ID", other.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());

    verify(userActivityService, never()).getUserActivity(any());
  }
}
