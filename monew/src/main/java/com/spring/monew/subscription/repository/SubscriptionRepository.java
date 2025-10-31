package com.spring.monew.subscription.repository;

import com.spring.monew.subscription.domain.Subscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

  Optional<Subscription> findByUser_IdAndInterest_Id(UUID userId, UUID interestId);

  boolean existsByUser_IdAndInterest_Id(UUID userId, UUID interestId);

  @Query("select s.user.id from Subscription s where s.interest.id = :interestId")
  List<UUID> findUserIdsByInterestId(UUID interestId);

  long countByInterest_Id(UUID interestId);
}