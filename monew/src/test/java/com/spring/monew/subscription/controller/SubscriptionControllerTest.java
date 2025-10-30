package com.spring.monew.subscription.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.config.TestSecurityConfig;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.service.SubscriptionService;
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

@Import(TestSecurityConfig.class)
@WebMvcTest(SubscriptionController.class)
@AutoConfigureMockMvc // Security Filter 비활성화
class SubscriptionControllerTest {

  @Autowired MockMvc mockMvc;
  @MockitoBean SubscriptionService subscriptionService;
  @MockitoBean
  RequestUserExtractor userExtractor;
  @Test
  @DisplayName("구독 등록 성공")
  void addSubscription() throws Exception {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    SubscriptionDto dto = new SubscriptionDto(
        UUID.randomUUID(), userId, "야구", List.of("스포츠"), 1L, Instant.now()
    );
    when(userExtractor.extractUserId(any())).thenReturn(userId);
    given(subscriptionService.addSubscription(interestId, userId)).willReturn(dto);

    mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interestId)
            .header("Monew-Request-User-ID", userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.interestName").value("야구"))
        .andExpect(jsonPath("$.interestSubscriberCount").value(1));
  }

  @Test
  @DisplayName("구독 해제 성공")
  void removeSubscription() throws Exception {
    UUID interestId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    willDoNothing().given(subscriptionService).removeSubscription(interestId, userId);

    mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interestId)
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk());
  }
}
