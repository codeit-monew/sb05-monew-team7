package com.spring.monew.subscription.repository;

import com.spring.monew.subscription.domain.Subscription;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

  Optional<Subscription> findByUser_IdAndInterest_Id(UUID userId, UUID interestId);

  boolean existsByUser_IdAndInterest_Id(UUID userId, UUID interestId);
}