package com.spring.monew.activity.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "activity_comments")
@CompoundIndexes({
    // 커서/정렬
    @CompoundIndex(name = "idx_actc_user_created_id_desc", def = "{'user_id': 1, 'created_at': -1, '_id': -1}")
})
public class ActivityCommentDoc {

  @Id
  private UUID id;  // comment_id

  @Indexed(name = "idx_actc_user")
  @Field("user_id")
  private UUID userId;  // 작성자

  @Field("article_id")
  private UUID articleId;

  @Field("article_title")
  private String articleTitle;

  @Field("user_nickname")
  private String userNickname;

  @Field("content")
  private String content;

  @Field("like_count")
  private long likeCount;

  @Field("created_at")
  private Instant createdAt;

  @Field("is_deleted")
  private Boolean isDeleted;

  @Field("deleted_at")
  private Instant deletedAt;
}
