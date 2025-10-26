package com.spring.monew.subscription.repository;

import com.spring.monew.subscription.domain.Subscription;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

  Optional<Subscription> findByInterest_IdAndUser_Id(UUID interestId, UUID userId);

  boolean existsByUser_IdAndInterest_Id(UUID userId, UUID interestId);
}