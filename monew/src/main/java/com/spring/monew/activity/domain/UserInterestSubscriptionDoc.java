package com.spring.monew.activity.domain;

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
// user_id, interest_id 로 인덱스 생성 + null 문서는 인덱스 적용 제외(부분 인덱스)
@CompoundIndexes({
    @CompoundIndex(
        name = "uk_sub_user_interest",
        def = "{'user_id': 1, 'interest_id': 1}",
        unique = true,
        partialFilter = "{ 'user_id': { $type: 'binData' }, 'interest_id': { $type: 'binData' } }"
    )
})
public class UserInterestSubscriptionDoc {
    @Id private String id;

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