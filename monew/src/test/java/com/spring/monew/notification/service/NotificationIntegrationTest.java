package com.spring.monew.notification.service;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.monew.notification.domain.Notification;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.repository.NotificationRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class NotificationIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;
  @Autowired private NotificationRepository notificationRepository;

  private User user;
  private Notification n1;
  private Notification n2;

  @BeforeEach
  void setup() {
    user = userRepository.save(new User("notify@test.com", "notifyNick", "pw"));
    n1 = Notification.of(user.getId(), "테스트 알림-기사",  NotificationResourceType.ARTICLE,  UUID.randomUUID());
    n2 = Notification.of(user.getId(), "테스트 알림-댓글", NotificationResourceType.COMMENT, UUID.randomUUID());
    notificationRepository.saveAll(List.of(n1, n2));
  }

  @Test
  @Order(1)
  @DisplayName("알림 목록 조회 성공")
  void listNotifications() throws Exception {
    mockMvc.perform(
            get("/api/notifications")
                .header("Monew-Request-User-ID", user.getId().toString())
                .param("limit", "50")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").exists())
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.nextAfter", notNullValue()))
        // enum은 @JsonValue로 소문자 문자열("article","comment")로 내려옴
        .andExpect(jsonPath("$.content[*].resourceType",
            containsInAnyOrder("article", "comment")))
        .andExpect(jsonPath("$.content[*].content",
            containsInAnyOrder("테스트 알림-기사", "테스트 알림-댓글")));
  }

  @Test
  @Order(2)
  @DisplayName("알림 단건 확인 성공")
  void confirmOne() throws Exception {
    UUID targetId = n1.getId();

    mockMvc.perform(
            patch("/api/notifications/{id}", targetId)
                .header("Monew-Request-User-ID", user.getId().toString())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(targetId.toString()))
        .andExpect(jsonPath("$.confirmed").value(true))
        .andExpect(jsonPath("$.alreadyConfirmed").value(false))
        .andExpect(jsonPath("$.userId").value(user.getId().toString()))
        .andExpect(jsonPath("$.updatedAt", notNullValue()));

    // 저장소에도 반영됐는지 확인(Optional)
    Notification updated = notificationRepository.findById(targetId).orElseThrow();
    org.assertj.core.api.Assertions.assertThat(updated.isConfirmed()).isTrue();
  }

  @Test
  @Order(3)
  @DisplayName("전체 알림 확인 성공")
  void confirmAll() throws Exception {
    mockMvc.perform(
            patch("/api/notifications")
                .header("Monew-Request-User-ID", user.getId().toString())
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.updatedCount").value(2))
        .andExpect(jsonPath("$.allConfirmed").value(true))
        .andExpect(jsonPath("$.userId").value(user.getId().toString()))
        .andExpect(jsonPath("$.processedAt").exists());

    // 전부 confirmed 되었는지 확인(Optional)
    org.assertj.core.api.Assertions.assertThat(
        notificationRepository.existsByUserIdAndConfirmedFalse(user.getId())
    ).isFalse();
  }

  @Test
  @Order(4)
  @DisplayName("인증 헤더 없으면 401")
  void listUnauthorized() throws Exception {
    mockMvc.perform(get("/api/notifications").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }
}
