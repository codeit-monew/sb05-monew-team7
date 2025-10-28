package com.spring.monew.subscription.controller;

import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.service.SubscriptionService;
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

  @PostMapping("/{interestId}/subscriptions")
  public SubscriptionDto subscriptionAdd(@PathVariable UUID interestId,
      @RequestHeader(name = "Monew-Request-User-ID") UUID userId) {
    return subscriptionService.addSubscription(interestId, userId);
  }

  @DeleteMapping("/{interestId}/subscriptions")
  public void subscriptionRemove(@PathVariable UUID interestId,
      @RequestHeader(name = "Monew-Request-User-ID") UUID userId) {
    subscriptionService.removeSubscription(interestId, userId);
  }
}
