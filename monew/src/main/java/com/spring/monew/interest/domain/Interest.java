package com.spring.monew.interest.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interests")
@NoArgsConstructor @AllArgsConstructor
@Getter @Builder
public class Interest {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "subscriptions_count", nullable = false)
    private long subscriptionsCount = 0L;
}
