package com.spring.monew.subscription.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.config.TestSecurityConfig;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.subscription.domain.Subscription;
import com.spring.monew.subscription.service.SubscriptionService;
import com.spring.monew.user.domain.User;
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

@WebMvcTest(SubscriptionController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class SubscriptionControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean SubscriptionService subscriptionService;
  @MockitoBean RequestUserExtractor userExtractor;
  @MockitoBean UserActivityService userActivityService;

  @Test
  @DisplayName("구독 등록 성공")
  void addSubscription_success() throws Exception {
    // given
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID subscriptionId = UUID.randomUUID();

    User user = new User("email@gmail.com", "user", "password");
    Interest interest = new Interest(
        UUID.randomUUID(),
        "야구",
        List.of("스포츠", "MLB", "KBO"),
        "스포츠,MLB,KBO",
        Instant.now(),
        3L
    );

    Subscription subscription = new Subscription(
        subscriptionId,
        user,
        interest,
        Instant.now()
    );

    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(subscriptionService.addSubscription(interestId, userId)).willReturn(subscription);

    // when & then
    mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
            .header("Monew-Request-User-ID", userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.interestName").value("야구"))
        .andExpect(jsonPath("$.interestKeywords[0]").value("스포츠"))
        .andExpect(jsonPath("$.interestSubscriberCount").value(3));

    verify(userActivityService).addSubscriptionActivity(any());
  }

  @Test
  @DisplayName("구독 해제 성공")
  void removeSubscription_success() throws Exception {
    // given
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID subscriptionId = UUID.randomUUID();

    User user = new User("email@gmail.com", "user", "password");
    Interest interest = new Interest(
        UUID.randomUUID(),
        "야구",
        List.of("스포츠", "MLB", "KBO"),
        "스포츠,MLB,KBO",
        Instant.now(),
        3L
    );

    Subscription subscription = new Subscription(
        subscriptionId,
        user,
        interest,
        Instant.now()
    );

    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(subscriptionService.removeSubscription(interestId, userId)).willReturn(subscription);

    // when & then
    mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk());

    verify(userActivityService).removeSubscriptionActivity(any());
  }
}
