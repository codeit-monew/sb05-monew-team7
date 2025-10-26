package com.spring.monew.subscription.service;

import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import java.util.UUID;

public interface SubscriptionService {

  SubscriptionDto addSubscription(UUID interestId, UUID userId);  // 관심사 ID, 요청자 ID[header]

  void removeSubscription(UUID interestId, UUID userId); // 관심사 ID, 요청자 ID[header]
}
