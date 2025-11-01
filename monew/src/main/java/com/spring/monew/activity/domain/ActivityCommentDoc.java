package com.spring.monew.activity.domain;

import java.time.Instant;
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
@Document("activity_comments")
@CompoundIndexes({
        @CompoundIndex(name = "uid_created_desc", def = "{userId:1, createdAt:-1, _id:-1}")
})
public class ActivityCommentDoc {
    @Id private String id;
    private UUID userId;
    private UUID articleId;
    private String articleTitle;
    private String content;
    private long likeCount;
    private Instant createdAt;
    @Field("user_nickname")
    private String userNickname;
}