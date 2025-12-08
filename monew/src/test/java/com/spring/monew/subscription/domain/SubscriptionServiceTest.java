package com.spring.monew.subscription.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.subscription.repository.SubscriptionRepository;
import com.spring.monew.subscription.service.impl.SubscriptionServiceImpl;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

  @Mock private SubscriptionRepository subscriptionRepository;
  @Mock private UserRepository userRepository;
  @Mock private InterestRepository interestRepository;

  @InjectMocks private SubscriptionServiceImpl subscriptionService;

  UUID userId;
  UUID interestId;

  User user;
  Interest interest;
  Subscription subscription;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    interestId = UUID.randomUUID();

    user = new User("email@test.com", "nickname", "password");

    interest =
        new Interest(
            interestId,
            "야구",
            List.of("스포츠", "KBO"),
            "스포츠,KBO",
            Instant.now(),
            1L);

    subscription = new Subscription(UUID.randomUUID(), user, interest, Instant.now());
  }

  @Test
  @DisplayName("구독 추가 성공")
  void addSubscription_success() {
    // given
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
    given(subscriptionRepository.existsByUser_IdAndInterest_Id(userId, interestId))
        .willReturn(false);
    given(subscriptionRepository.save(any(Subscription.class))).willReturn(subscription);

    // when
    Subscription result = subscriptionService.addSubscription(interestId, userId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(user);
    assertThat(result.getInterest()).isEqualTo(interest);
    verify(subscriptionRepository).save(any(Subscription.class));
  }

  @Test
  @DisplayName("구독 추가 실패 - 존재하지 않는 유저")
  void addSubscription_userNotFound() {
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> subscriptionService.addSubscription(interestId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지 않는 유저");
  }

  @Test
  @DisplayName("구독 추가 실패 - 존재하지 않는 관심사")
  void addSubscription_interestNotFound() {
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(interestRepository.findById(interestId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> subscriptionService.addSubscription(interestId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지 않는 관심사");
  }

  @Test
  @DisplayName("구독 추가 실패 - 이미 존재하는 구독")
  void addSubscription_alreadyExists() {
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
    given(subscriptionRepository.existsByUser_IdAndInterest_Id(userId, interestId))
        .willReturn(true);

    assertThatThrownBy(() -> subscriptionService.addSubscription(interestId, userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("이미 존재하는 구독");
  }

  @Test
  @DisplayName("구독 제거 성공")
  void removeSubscription_success() {
    // given
    given(subscriptionRepository.findByUser_IdAndInterest_Id(userId, interestId))
        .willReturn(Optional.of(subscription));

    // when
    Subscription result = subscriptionService.removeSubscription(interestId, userId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(user);
    assertThat(result.getInterest()).isEqualTo(interest);
    verify(subscriptionRepository).delete(subscription);
  }

  @Test
  @DisplayName("구독 제거 실패 - 존재하지 않는 구독")
  void removeSubscription_notFound() {
    given(subscriptionRepository.findByUser_IdAndInterest_Id(userId, interestId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> subscriptionService.removeSubscription(interestId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("관심사 또는 유저가 존재하지 않습니다");
  }
}
