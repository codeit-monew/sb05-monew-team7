package com.spring.monew.subscription.service;

import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.domain.Subscription;
import java.util.UUID;

public interface SubscriptionService {

  Subscription addSubscription(UUID interestId, UUID userId);  // 관심사 ID, 요청자 ID[header]

  Subscription removeSubscription(UUID interestId, UUID userId); // 관심사 ID, 요청자 ID[header]
}
