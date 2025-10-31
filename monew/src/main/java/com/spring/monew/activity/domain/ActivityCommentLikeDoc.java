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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("activity_comment_likes")
@CompoundIndexes({
        @CompoundIndex(name = "uid_created_desc", def = "{userId:1, createdAt:-1, _id:-1}")
})
public class ActivityCommentLikeDoc {
    @Id private String id; // likeEventId
    private UUID userId; // likedBy
    private UUID commentId;
    private UUID articleId;
    private String articleTitle;
    private UUID commentUserId;
    private String commentUserNickname;
    private String commentContent;
    private long commentLikeCount;
    private Instant commentCreatedAt;
    private Instant createdAt; // like time
}