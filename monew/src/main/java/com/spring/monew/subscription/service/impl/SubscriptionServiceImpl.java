package com.spring.monew.subscription.service.impl;

import com.spring.monew.activity.repository.ActivitySyncRepository;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.domain.Subscription;
import com.spring.monew.subscription.repository.SubscriptionRepository;
import com.spring.monew.subscription.service.SubscriptionService;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final UserRepository userRepository;
  private final InterestRepository interestRepository;
  private final ActivitySyncRepository activitySyncRepository;

  @Override
  @Transactional
  public SubscriptionDto addSubscription(UUID interestId, UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 유저입니다."));

    Interest interest = interestRepository.findById(interestId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 관심사 입니다."));

    if(subscriptionRepository.existsByUser_IdAndInterest_Id(userId, interestId)) {
      throw new IllegalArgumentException("이미 존재하는 구독 입니다.");
    }
    // 구독자 수 증가 카운터
    interest.incrementSubscriptionsCount();

    Subscription subscription = subscriptionRepository.save(new Subscription(user, interest));

    try {
      activitySyncRepository.onSubscribed(
          subscription.getId(),
          user.getId(),
          interest.getId(),
          interest.getName(),
          interest.getKeywords(),
          subscriptionRepository.countByInterest_Id(interest.getId()),
          subscription.getCreatedAt()
      );
    } catch (Exception ignore) {}

    return new SubscriptionDto(
        subscription.getId(),
        subscription.getUser().getId(),
        subscription.getInterest().getName(),
        subscription.getInterest().getKeywords(),
        subscription.getInterest().getSubscriptionsCount(),
        subscription.getCreatedAt());
  }

  @Override
  @Transactional
  public void removeSubscription(UUID interestId, UUID userId) {
    Subscription subscription = subscriptionRepository
        .findByUser_IdAndInterest_Id(userId, interestId)
        .orElseThrow(() -> new NoSuchElementException("관심사 또는 유저가 존재하지 않습니다."));

    // 구독자 수 감소 로직
    subscription.getInterest().decrementSubscriptionsCount();

    subscriptionRepository.delete(subscription);
    try {
      activitySyncRepository.onUnsubscribed(subscription.getId());
    } catch (Exception ignore) {}
  }
}