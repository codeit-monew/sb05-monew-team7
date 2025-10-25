package com.spring.monew.subscription.service.impl;

import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.repository.SubscriptionRepository;
import com.spring.monew.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;

  @Override
  public SubscriptionDto addSubscription() {
    return null;
  }

  @Override
  public void removeSubscription() {

  }
}
