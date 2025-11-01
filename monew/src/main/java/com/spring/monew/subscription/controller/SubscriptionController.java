package com.spring.monew.subscription.controller;

import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.notification.service.NotificationService;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.domain.Subscription;
import com.spring.monew.subscription.service.SubscriptionService;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class SubscriptionController {
  private final SubscriptionService subscriptionService;
  private final RequestUserExtractor userExtractor;
  private final UserActivityService userActivityService;

  @PostMapping("/{interestId}/subscriptions")
  public SubscriptionDto subscriptionAdd(@PathVariable UUID interestId,
      Principal principal) {

    UUID userId = userExtractor.extractUserId(principal);
    Subscription subscription = subscriptionService.addSubscription(interestId, userId);

    userActivityService.addSubscriptionActivity(subscription);

    return new SubscriptionDto(
        subscription.getId(),
        subscription.getUser().getId(),
        subscription.getInterest().getName(),
        subscription.getInterest().getKeywords(),
        subscription.getInterest().getSubscriptionsCount(),
        subscription.getCreatedAt());
  }

  @DeleteMapping("/{interestId}/subscriptions")
  public void subscriptionRemove(@PathVariable UUID interestId,
      Principal principal) {
    UUID userId = userExtractor.extractUserId(principal);
    Subscription subscription = subscriptionService.removeSubscription(interestId, userId);

    userActivityService.removeSubscriptionActivity(subscription.getId());
  }
}
