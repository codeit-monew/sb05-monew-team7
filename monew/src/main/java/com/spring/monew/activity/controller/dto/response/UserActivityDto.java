package com.spring.monew.activity.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(name = "UserActivityDto")
public record UserActivityDto(
    UUID id,
    String email,
    String nickname,
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,
    List<Subscription> subscriptions,
    List<Comment> comments,
    List<CommentLike> commentLikes,
    List<ArticleView> articleViews
) {
  @Schema(name = "UserActivityDto.Subscription")
  public static record Subscription(
      UUID id,
      UUID interestId,
      String interestName,
      List<String> interestKeywords,
      long interestSubscriberCount,
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt
  ) {}

  @Schema(name = "UserActivityDto.Comment")
  public static record Comment(
      UUID id,
      UUID articleId,
      String articleTitle,
      UUID userId,
      String userNickname,
      String content,
      long likeCount,
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt
  ) {}

  // 스웨거 예시와 맞춤: 내가 누른 좋아요 항목에서 '대상 댓글'의 스냅샷 정보 제공
  @Schema(name = "UserActivityDto.CommentLike")
  public static record CommentLike(
      UUID id, // likeEventId
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,
      UUID commentId,
      UUID articleId,
      String articleTitle,
      UUID commentUserId,
      String commentUserNickname,
      String commentContent,
      long commentLikeCount,
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant commentCreatedAt
  ) {}

  @Schema(name = "UserActivityDto.ArticleView")
  public static record ArticleView(
      UUID id,
      UUID viewedBy, // userId
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,
      UUID articleId,
      String source,
      String sourceUrl,
      String articleTitle,
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant articlePublishedDate,
      String articleSummary,
      long commentCount,
      long viewCount
  ) {}
}