package com.spring.monew.interest.domain;

import com.spring.monew.common.converter.KeywordsConverter;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "interests")
@NoArgsConstructor @AllArgsConstructor
@Getter @Builder
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)   //PK 자동 삽입
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Convert(converter = KeywordsConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<String> keywords;

    @Column(name = "subscriptions_count", nullable = false)
    @ColumnDefault("0")
    private long subscriptionsCount;
}
