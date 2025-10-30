package com.spring.monew.activity.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Document(collection = "user_interest_subscriptions")
@CompoundIndexes({
    // 커서/정렬
    @CompoundIndex(name = "idx_sub_user_created_id_desc", def = "{'user_id': 1, 'created_at': -1, '_id': -1}"),
    // 유니크 제약
    @CompoundIndex(name = "uk_sub_user_interest", def = "{'user_id': 1, 'interest_id': 1}", unique = true)
})
public class UserInterestSubscriptionDoc {

    @Id
    private final UUID id;

    @Indexed(name = "idx_sub_user")
    @Field("user_id")
    private final UUID userId;

    @Field("interest_id")
    private final UUID interestId;

    @Field("interest_name")
    private final String interestName;

    @Field("interest_keywords")
    private final List<String> interestKeywords;

    @Field("interest_subscriber_count")
    private final long interestSubscriberCount;

    @Field("created_at")
    private final Instant createdAt;

    // === Spring Data가 사용할 생성자 ===
    @PersistenceCreator
    public UserInterestSubscriptionDoc(
        UUID id,
        UUID userId,
        UUID interestId,
        String interestName,
        List<String> interestKeywords,
        long interestSubscriberCount,
        Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.interestId = interestId;
        this.interestName = interestName;
        this.interestKeywords = interestKeywords;
        this.interestSubscriberCount = interestSubscriberCount;
        this.createdAt = createdAt;
    }

    // === 정적 팩토리 ===
    // 구독 최초 생성
    public static UserInterestSubscriptionDoc create(
        UUID subscriptionId,
        UUID userId,
        UUID interestId,
        String interestName,
        List<String> interestKeywords,
        long interestSubscriberCount,
        Instant createdAt
    ) {
        return new UserInterestSubscriptionDoc(
            subscriptionId,
            userId,
            interestId,
            interestName,
            interestKeywords,
            interestSubscriberCount,
            createdAt != null ? createdAt : Instant.now()
        );
    }

    // 스냅샷을 모두 지정해 복원/마이그레이션
    public static UserInterestSubscriptionDoc of(
        UUID id,
        UUID userId,
        UUID interestId,
        String interestName,
        List<String> interestKeywords,
        long interestSubscriberCount,
        Instant createdAt
    ) {
        return new UserInterestSubscriptionDoc(
            id, userId, interestId, interestName, interestKeywords, interestSubscriberCount, createdAt
        );
    }

    // 관심사 메타만 갱신한 새 인스턴스
    public UserInterestSubscriptionDoc withInterestSnapshot(
        String interestName,
        List<String> interestKeywords,
        Long interestSubscriberCount
    ) {
        return new UserInterestSubscriptionDoc(
            this.id,
            this.userId,
            this.interestId,
            interestName != null ? interestName : this.interestName,
            interestKeywords != null ? interestKeywords : this.interestKeywords,
            interestSubscriberCount != null ? interestSubscriberCount : this.interestSubscriberCount,
            this.createdAt
        );
    }
}