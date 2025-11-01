package com.spring.monew.activity.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

  @Autowired
  MockMvc mvc;

  @MockitoBean
  UserActivityService userActivityService;

  @MockitoBean
  RequestUserExtractor userExtractor;

  private UserActivityDto sampleDto(UUID userId) {
    return new UserActivityDto(
        userId,
        "user@example.com",
        "유저",
        Instant.parse("2025-01-01T00:00:00Z"),
        List.<SubscriptionDto>of(),
        List.<CommentActivityDto>of(),
        List.<CommentLikeActivityDto>of(),
        List.<ArticleViewDto>of()
    );
  }

  @Test
  @DisplayName("내 활동 조회 성공")
  void myActivity_success() throws Exception {
    UUID me = UUID.randomUUID();
    when(userExtractor.extractUserId(any())).thenReturn(me);
    when(userActivityService.getUserActivity(me)).thenReturn(sampleDto(me));

    mvc.perform(get("/api/user-activities/me")
            .header("Monew-Request-User-ID", me.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(me.toString())))
        .andExpect(jsonPath("$.email", is("user@example.com")))
        .andExpect(jsonPath("$.nickname", is("유저")));

    verify(userActivityService).getUserActivity(eq(me));
  }

  @Test
  @DisplayName("내 활동 조회 - 헤더 없음")
  void myActivity_unauthorized_when_no_header() throws Exception {
    when(userExtractor.extractUserId(any())).thenReturn(null);

    mvc.perform(get("/api/user-activities/me"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("사용자 활동 조회(본인) 성공")
  void userActivityById_owner_success() throws Exception {
    UUID me = UUID.randomUUID();
    when(userExtractor.extractUserId(any())).thenReturn(me);
    when(userActivityService.getUserActivity(me)).thenReturn(sampleDto(me));

    mvc.perform(get("/api/user-activities/{userId}", me)
            .header("Monew-Request-User-ID", me.toString())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", is(me.toString())))
        .andExpect(jsonPath("$.email", is("user@example.com")))
        .andExpect(jsonPath("$.nickname", is("유저")));

    verify(userActivityService).getUserActivity(eq(me));
  }

  @Test
  @DisplayName("사용자 활동 조회(타인)")
  void userActivityById_forbidden_when_other() throws Exception {
    UUID requester = UUID.randomUUID();
    UUID target = UUID.randomUUID();
    when(userExtractor.extractUserId(any())).thenReturn(requester);

    mvc.perform(get("/api/user-activities/{userId}", target)
            .header("Monew-Request-User-ID", requester.toString()))
        .andExpect(status().isForbidden());
  }
}