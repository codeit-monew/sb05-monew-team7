package com.spring.monew.subscription.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.repository.SubscriptionRepository;
import com.spring.monew.subscription.service.impl.SubscriptionServiceImpl;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

  @Mock
  SubscriptionRepository subscriptionRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  InterestRepository interestRepository;
  @InjectMocks
  SubscriptionServiceImpl subscriptionService;

  @Test
  @DisplayName("구독 등록 성공 시 SubscriptionDto 반환")
  void addSubscription_success() {
    // given
    UUID userId = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();

    User user = new User("user@test.com", "테스트유저", "password");
    Interest interest = new Interest("야구", List.of("스포츠"));

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(interestRepository.findById(interestId)).willReturn(Optional.of(interest));
    given(subscriptionRepository.existsByUser_IdAndInterest_Id(userId, interestId)).willReturn(
        false);
    given(subscriptionRepository.save(any(Subscription.class))).willAnswer(
        inv -> inv.getArgument(0));

    // when
    SubscriptionDto dto = subscriptionService.addSubscription(interestId, userId);

    // then
    assertThat(dto.interestName()).isEqualTo("야구");
    assertThat(interest.getSubscriptionsCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("이미 구독한 경우 IllegalArgumentException 발생")
  void addSubscription_duplicate() {
    UUID userId = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.of(new User("user@test.com", "유저", "password")));
    given(interestRepository.findById(interestId)).willReturn(
        Optional.of(new Interest("야구", List.of("스포츠"))));
    given(subscriptionRepository.existsByUser_IdAndInterest_Id(userId, interestId)).willReturn(
        true);

    assertThatThrownBy(() -> subscriptionService.addSubscription(interestId, userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("이미 존재하는 구독 입니다.");
  }

  @Test
  @DisplayName("존재하지 않는 관심사일 경우 NoSuchElementException 발생")
  void addSubscription_interestNotFound() {
    UUID userId = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.of(new User("user@test.com", "유저", "password")));
    given(interestRepository.findById(interestId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> subscriptionService.addSubscription(interestId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("존재하지 않는 관심사 입니다.");
  }

  @Test
  @DisplayName("구독 해제 시 Interest 구독자 수 감소")
  void removeSubscription_success() {
    UUID userId = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();

    Interest interest = new Interest("야구", List.of("스포츠"));
    interest.incrementSubscriptionsCount();
    Subscription subscription = new Subscription(new User("u@test.com", "테스트", "password"),
        interest);

    given(subscriptionRepository.findByUser_IdAndInterest_Id(userId, interestId)).willReturn(
        Optional.of(subscription));

    subscriptionService.removeSubscription(interestId, userId);

    verify(subscriptionRepository).delete(subscription);
    assertThat(interest.getSubscriptionsCount()).isZero();
  }
}
