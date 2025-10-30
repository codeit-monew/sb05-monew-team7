package com.spring.monew.activity.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "user_interest_subscriptions")
@CompoundIndexes({
    // 커서/정렬
    @CompoundIndex(name = "idx_sub_user_created_id_desc", def = "{'user_id': 1, 'created_at': -1, '_id': -1}"),
    // 유니크 제약
    @CompoundIndex(name = "uk_sub_user_interest", def = "{'user_id': 1, 'interest_id': 1}", unique = true)
})
public class UserInterestSubscriptionDoc {

    @Id
    private UUID id;

    @Indexed(name = "idx_sub_user")
    @Field("user_id")
    private UUID userId;

    @Field("interest_id")
    private UUID interestId;

    @Field("interest_name")
    private String interestName;

    @Field("interest_keywords")
    private List<String> interestKeywords;

    @Field("interest_subscriber_count")
    private long interestSubscriberCount;

    @Field("created_at")
    private Instant createdAt;
}