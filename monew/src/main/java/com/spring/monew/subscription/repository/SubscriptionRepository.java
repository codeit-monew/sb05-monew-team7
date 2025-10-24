package com.spring.monew.subscription.repository;

import com.spring.monew.subscription.domain.Subscription;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

}