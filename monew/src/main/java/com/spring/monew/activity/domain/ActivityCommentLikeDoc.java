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
  private UUID id;                                  // like 이벤트 ID

  @Indexed(name = "idx_actl_user")
  @Field("user_id")
  private UUID userId;                              // 좋아요 누른 사용자

  @Indexed(name = "idx_actl_comment")
  @Field("comment_id")
  private UUID commentId;

  @Indexed(name = "idx_actl_article")
  @Field("article_id")
  private UUID articleId;

  @Field("article_title")
  private String articleTitle;

  @Indexed(name = "idx_actl_comment_user")
  @Field("comment_user_id")
  private UUID commentUserId;

  @Field("comment_user_nickname")
  private String commentUserNickname;

  @Field("comment_content")
  private String commentContent;

  @Field("comment_like_count")
  private long commentLikeCount;

  @Field("comment_created_at")
  private Instant commentCreatedAt;

  @Field("created_at")
  private Instant createdAt;

  // ---------- 정적 팩토리(값 정규화) ----------
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
    ActivityCommentLikeDoc doc = new ActivityCommentLikeDoc();
    doc.setId(id);
    doc.setUserId(userId);
    doc.setCommentId(commentId);
    doc.setArticleId(articleId);
    doc.setArticleTitle(articleTitle);
    doc.setCommentUserId(commentUserId);
    doc.setCommentUserNickname(commentUserNickname);
    doc.setCommentContent(commentContent);
    doc.setCommentLikeCount(commentLikeCount);
    doc.setCommentCreatedAt(commentCreatedAt);
    doc.setCreatedAt(createdAt != null ? createdAt : Instant.now());
    return doc;
  }
}