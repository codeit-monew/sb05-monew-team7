package com.spring.monew.subscription.controller;

import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
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
  @PostMapping("/{interestId}/subscriptions")
  public SubscriptionDto subscriptionAdd(@PathVariable UUID interestId,
      Principal principal) {

    UUID userId = userExtractor.extractUserId(principal);
    return subscriptionService.addSubscription(interestId, userId);
  }

  @DeleteMapping("/{interestId}/subscriptions")
  public void subscriptionRemove(@PathVariable UUID interestId,
      Principal principal) {
    UUID userId = userExtractor.extractUserId(principal);
    subscriptionService.removeSubscription(interestId, userId);
  }
}
