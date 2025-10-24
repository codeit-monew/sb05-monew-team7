package com.spring.monew.subscribe.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)   //PK 자동 삽입
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "interest_id", nullable = false)
    private UUID interestId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}
