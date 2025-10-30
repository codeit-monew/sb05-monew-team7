package com.spring.monew.activity.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "UserActivityDto", description = "사용자 활동 스냅샷 응답")
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
  // 내부 레코드: public 제거 (package-private), static 표기는 불필요
  @Schema(name = "UserActivityDto.Subscription")
  record Subscription(
      UUID id,
      UUID interestId,
      String interestName,
      List<String> interestKeywords,
      long interestSubscriberCount,
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt
  ) {}

  @Schema(name = "UserActivityDto.Comment")
  record Comment(
      UUID id,
      UUID articleId,
      String articleTitle,
      UUID userId,
      String userNickname,
      String content,
      long likeCount,
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt
  ) {}

  @Schema(name = "UserActivityDto.CommentLike")
  record CommentLike(
      UUID id,
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
  record ArticleView(
      UUID id,
      UUID viewedBy,
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,
      UUID articleId,
      String source,
      String sourceUrl,
      String articleTitle,
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant articlePublishedDate,
      String articleSummary,
      long articleCommentCount,
      long articleViewCount
  ) {}
}