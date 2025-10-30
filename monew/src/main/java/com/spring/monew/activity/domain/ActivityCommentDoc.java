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
@Document(collection = "activity_comments")
@CompoundIndexes({
    // 커서/정렬: created_at DESC, _id DESC
    @CompoundIndex(name = "idx_actc_user_created_id_desc", def = "{'user_id': 1, 'created_at': -1, '_id': -1}")
})
public class ActivityCommentDoc {

  @Id
  private final UUID id;                 // comment_id

  @Indexed(name = "idx_actc_user")
  @Field("user_id")
  private final UUID userId;             // 작성자

  @Field("article_id")
  private final UUID articleId;

  @Field("article_title")
  private final String articleTitle;

  @Field("user_nickname")
  private final String userNickname;

  @Field("content")
  private final String content;

  @Field("like_count")
  private final long likeCount;

  @Field("created_at")
  private final Instant createdAt;

  @Field("is_deleted")
  private final Boolean isDeleted;

  @Field("deleted_at")
  private final Instant deletedAt;

  // === Spring Data가 사용할 생성자 ===
  @PersistenceCreator
  public ActivityCommentDoc(
      UUID id,
      UUID userId,
      UUID articleId,
      String articleTitle,
      String userNickname,
      String content,
      long likeCount,
      Instant createdAt,
      Boolean isDeleted,
      Instant deletedAt
  ) {
    this.id = id;
    this.userId = userId;
    this.articleId = articleId;
    this.articleTitle = articleTitle;
    this.userNickname = userNickname;
    this.content = content;
    this.likeCount = likeCount;
    this.createdAt = createdAt;
    this.isDeleted = isDeleted;
    this.deletedAt = deletedAt;
  }

  // === 정적 팩토리 ===

  // 댓글 최초 생성
  public static ActivityCommentDoc create(
      UUID commentId,
      UUID userId,
      UUID articleId,
      String articleTitleSnapshot,
      String userNicknameSnapshot,
      String content,
      long likeCountSnapshot,
      Instant createdAt
  ) {
    return new ActivityCommentDoc(
        commentId,
        userId,
        articleId,
        articleTitleSnapshot,
        userNicknameSnapshot,
        content,
        likeCountSnapshot,
        createdAt != null ? createdAt : Instant.now(),
        false,
        null
    );
  }

  // 스냅샷을 모두 지정해 복원/마이그레이션
  public static ActivityCommentDoc of(
      UUID id,
      UUID userId,
      UUID articleId,
      String articleTitle,
      String userNickname,
      String content,
      long likeCount,
      Instant createdAt,
      Boolean isDeleted,
      Instant deletedAt
  ) {
    return new ActivityCommentDoc(
        id, userId, articleId, articleTitle, userNickname,
        content, likeCount, createdAt, isDeleted, deletedAt
    );
  }

  // === 불변 업데이트(새 인스턴스 반환) ===
  // 소프트 삭제 표시 */
  public ActivityCommentDoc softDeleted(Instant when) {
    return new ActivityCommentDoc(
        this.id, this.userId, this.articleId, this.articleTitle, this.userNickname,
        this.content, this.likeCount, this.createdAt,
        true, when != null ? when : Instant.now()
    );
  }

  // 좋아요 수 스냅샷 갱신
  public ActivityCommentDoc withLikeCount(long newLikeCount) {
    return new ActivityCommentDoc(
        this.id, this.userId, this.articleId, this.articleTitle, this.userNickname,
        this.content, newLikeCount, this.createdAt, this.isDeleted, this.deletedAt
    );
  }

  // 댓글 본문/표시 스냅샷 일부 갱신
  public ActivityCommentDoc withSnapshot(String articleTitle, String userNickname, String content) {
    return new ActivityCommentDoc(
        this.id, this.userId, this.articleId,
        articleTitle != null ? articleTitle : this.articleTitle,
        userNickname != null ? userNickname : this.userNickname,
        content != null ? content : this.content,
        this.likeCount, this.createdAt, this.isDeleted, this.deletedAt
    );
  }
}
