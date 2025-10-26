package com.spring.monew.interest.domain;

import com.spring.monew.common.converter.KeywordsConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "interests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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

    @Builder
    public Interest(String name, List<String> keywords) {
        this.name = name;
        this.keywords = keywords;
        this.subscriptionsCount = 0L; // 기본값 설정
    }
}
