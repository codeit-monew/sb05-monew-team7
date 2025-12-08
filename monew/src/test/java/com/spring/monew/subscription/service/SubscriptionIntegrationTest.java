package com.spring.monew.subscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.subscription.domain.Subscription;
import com.spring.monew.subscription.repository.SubscriptionRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class SubscriptionIntegrationTest {

  @Autowired
  MockMvc mockMvc;
  @Autowired
  UserRepository userRepository;
  @Autowired
  InterestRepository interestRepository;
  @Autowired
  SubscriptionRepository subscriptionRepository;

  @Test
  @DisplayName("구독 등록 성공")
  void addSubscription_success() throws Exception {
    // given
    User user = userRepository.save(new User("email@test.com", "nick", "password"));
    Interest interest = interestRepository.save(new Interest("야구", List.of("스포츠", "리그")));

    // when
    mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interest.getId())
            .header("Monew-Request-User-ID", user.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    // then
    boolean exists = subscriptionRepository.existsByUser_IdAndInterest_Id(user.getId(),
        interest.getId());
    assertThat(exists).isTrue();

    Interest updated = interestRepository.findById(interest.getId()).orElseThrow();
    assertThat(updated.getSubscriptionsCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("중복 구독 시 예외 발생")
  void addSubscription_duplicate() throws Exception {
    // given
    User user = userRepository.save(new User("email@test.com", "nick", "password"));
    Interest interest = interestRepository.save(new Interest("야구", List.of("스포츠", "리그")));
    subscriptionRepository.save(new Subscription(user, interest));

    // when & then
    mockMvc.perform(post("/api/interests/{interestId}/subscriptions", interest.getId())
            .header("Monew-Request-User-ID", user.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("구독 해제 성공")
  void removeSubscription_success() throws Exception {
    // given
    User user = userRepository.save(new User("email@test.com", "nick", "password"));
    Interest interest = interestRepository.save(new Interest("야구", List.of("스포츠", "리그")));
    Subscription subscription = subscriptionRepository.save(new Subscription(user, interest));

    // when
    mockMvc.perform(delete("/api/interests/{interestId}/subscriptions", interest.getId())
            .header("Monew-Request-User-ID", user.getId()))
        .andExpect(status().isOk());

    // then
    boolean exists = subscriptionRepository.existsById(subscription.getId());
    assertThat(exists).isFalse();

    Interest updated = interestRepository.findById(interest.getId()).orElseThrow();
    assertThat(updated.getSubscriptionsCount()).isEqualTo(0L);
  }

  @Test
  @DisplayName("존재하지 않는 관심사로 구독 요청 시 예외")
  void addSubscription_interestNotFound() throws Exception {
    // given
    User user = userRepository.save(new User("email@test.com", "nick", "password"));
    UUID fakeInterestId = UUID.randomUUID();

    // when & then
    mockMvc.perform(post("/api/interests/{interestId}/subscriptions", fakeInterestId)
            .header("Monew-Request-User-ID", user.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}