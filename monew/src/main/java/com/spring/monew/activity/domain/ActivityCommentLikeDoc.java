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
import java.util.UUID;

@Getter
@Document(collection = "activity_comment_likes")
@CompoundIndexes({
    // 사용자별 최신순 목록 커서/정렬: created_at DESC, _id DESC
    @CompoundIndex(
        name = "idx_actl_user_created_id_desc",
        def  = "{'user_id': 1, 'created_at': -1, '_id': -1}"
    )
})
public class ActivityCommentLikeDoc {

  @Id
  private final UUID id;                                  // like 이벤트 ID

  @Indexed(name = "idx_actl_user")
  @Field("user_id")
  private final UUID userId;                              // 좋아요 누른 사용자

  @Indexed(name = "idx_actl_comment")
  @Field("comment_id")
  private final UUID commentId;

  @Indexed(name = "idx_actl_article")
  @Field("article_id")
  private final UUID articleId;

  @Field("article_title")
  private final String articleTitle;

  @Indexed(name = "idx_actl_comment_user")
  @Field("comment_user_id")
  private final UUID commentUserId;

  @Field("comment_user_nickname")
  private final String commentUserNickname;

  @Field("comment_content")
  private final String commentContent;

  @Field("comment_like_count")
  private final long commentLikeCount;

  @Field("comment_created_at")
  private final Instant commentCreatedAt;

  @Field("created_at")
  private final Instant createdAt;

  // === Spring Data가 사용할 생성자 ===
  @PersistenceCreator
  public ActivityCommentLikeDoc(
      UUID id,
      UUID userId,
      UUID commentId,
      UUID articleId,
      String articleTitle,
      UUID commentUserId,
      String commentUserNickname,
      String commentContent,
      long commentLikeCount,
      Instant commentCreatedAt,
      Instant createdAt
  ) {
    this.id = id;
    this.userId = userId;
    this.commentId = commentId;
    this.articleId = articleId;
    this.articleTitle = articleTitle;
    this.commentUserId = commentUserId;
    this.commentUserNickname = commentUserNickname;
    this.commentContent = commentContent;
    this.commentLikeCount = commentLikeCount;
    this.commentCreatedAt = commentCreatedAt;
    this.createdAt = createdAt;
  }

  // === 정적 팩토리 ===

  /** 이벤트 로그 생성(널 createdAt이면 now) */
  public static ActivityCommentLikeDoc of(
      UUID id,
      UUID userId,
      UUID commentId,
      UUID articleId,
      String articleTitle,
      UUID commentUserId,
      String commentUserNickname,
      String commentContent,
      long commentLikeCount,
      Instant commentCreatedAt,
      Instant createdAt
  ) {
    return new ActivityCommentLikeDoc(
        id,
        userId,
        commentId,
        articleId,
        articleTitle,
        commentUserId,
        commentUserNickname,
        commentContent,
        commentLikeCount,
        commentCreatedAt,
        createdAt != null ? createdAt : Instant.now()
    );
  }

  /** 스냅샷 일부만 갱신한 새 인스턴스 (선택) */
  public ActivityCommentLikeDoc withSnapshot(
      String articleTitle,
      String commentUserNickname,
      String commentContent,
      Long commentLikeCount
  ) {
    return new ActivityCommentLikeDoc(
        this.id,
        this.userId,
        this.commentId,
        this.articleId,
        articleTitle != null ? articleTitle : this.articleTitle,
        this.commentUserId,
        commentUserNickname != null ? commentUserNickname : this.commentUserNickname,
        commentContent != null ? commentContent : this.commentContent,
        commentLikeCount != null ? commentLikeCount : this.commentLikeCount,
        this.commentCreatedAt,
        this.createdAt
    );
  }
}