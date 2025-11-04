package com.spring.monew.interest.domain;

import com.spring.monew.common.converter.KeywordsConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import org.jetbrains.annotations.TestOnly;

@Entity
@Table(name = "interests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE interests SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Convert(converter = KeywordsConverter.class)
    @Column(nullable = false, columnDefinition = "TEXT")
    private List<String> keywords;

    @Column(name = "keywords", insertable = false, updatable = false)
    private String keywordsString;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "subscriptions_count", nullable = false)
    @ColumnDefault("0")
    private long subscriptionsCount;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public Interest(String name, List<String> keywords) {
        this.name = name;
        this.keywords = keywords;
        this.createdAt = Instant.now();
        this.subscriptionsCount = 0L;
    }

    public void update(List<String> keywords) {
        if (keywords != null && !keywords.isEmpty()) this.keywords = keywords;
    }

    public void incrementSubscriptionsCount() {
        this.subscriptionsCount++;
    }

    public void decrementSubscriptionsCount() {
        if (this.subscriptionsCount > 0) {
            this.subscriptionsCount--;
        }
    }

    public void undelete() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    @TestOnly
    public Interest(UUID id, String name, List<String> keywords, String keywordsString,
        Instant createdAt, long subscriptionsCount) {
        this.id = id;
        this.name = name;
        this.keywords = keywords;
        this.keywordsString = keywordsString;
        this.createdAt = createdAt;
        this.subscriptionsCount = subscriptionsCount;
    }
}
