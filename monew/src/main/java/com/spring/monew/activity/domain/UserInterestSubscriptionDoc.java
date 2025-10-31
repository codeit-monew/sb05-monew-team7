package com.spring.monew.activity.domain;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("user_interest_subscriptions")
@CompoundIndexes({
    @CompoundIndex(
        name = "uk_sub_user_interest",
        def = "{'user_id': 1, 'interest_id': 1}",
        unique = true,
        // 유니크 인덱스는 UUID(binData)기반 문서만 대상으로 함
        partialFilter = "{ 'user_id': { $type: 'binData' }, 'interest_id': { $type: 'binData' } }"
    )
})
public class UserInterestSubscriptionDoc {

    @Id
    private String id;

    @NotNull
    @Field("user_id")
    private UUID userId;

    @NotNull
    @Field("interest_id")
    private UUID interestId;

    @Field("interest_name")
    private String interestName;

    @Field("interest_keywords")
    private List<String> interestKeywords;

    @PositiveOrZero
    @Field("interest_subscriber_count")
    private long interestSubscriberCount;

    @NotNull
    @Field("created_at")
    private Instant createdAt;
}