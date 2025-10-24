package com.spring.monew.subscription.service;

import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;

public interface SubscriptionService {

  SubscriptionDto addSubscription();  // 관심사 ID, 요청자 ID[header]

  void removeSubscription(); // 관심사 ID, 요청자 ID[header]
}
