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
    @Schema(description = "사용자 ID") UUID id,
    @Schema(description = "사용자 이메일") String email,
    @Schema(description = "사용자 닉네임") String nickname,
    @Schema(description = "사용자 생성 시각(UTC ISO-8601)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,

    @Schema(description = "구독 중인 관심사 목록") List<Subscription> subscriptions,
    @Schema(description = "사용자가 작성한 댓글 목록") List<Comment> comments,
    @Schema(description = "사용자가 누른 댓글 좋아요 이력") List<CommentLike> commentLikes,
    @Schema(description = "사용자의 기사 열람 이력") List<ArticleView> articleViews
) {

  @Schema(name = "UserActivityDto.Subscription")
  public static record Subscription(
      @Schema(description = "구독 레코드 ID") UUID id,
      @Schema(description = "관심사 ID") UUID interestId,
      @Schema(description = "관심사 이름") String interestName,
      @Schema(description = "관심사 키워드 목록") List<String> interestKeywords,
      @Schema(description = "관심사 전체 구독자 수(스냅샷)") long interestSubscriberCount,
      @Schema(description = "구독 생성 시각(UTC ISO-8601)")
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt
  ) {}

  @Schema(name = "UserActivityDto.Comment")
  public static record Comment(
      @Schema(description = "댓글 ID") UUID id,
      @Schema(description = "대상 기사 ID") UUID articleId,
      @Schema(description = "대상 기사 제목") String articleTitle,
      @Schema(description = "댓글 작성자 ID") UUID userId,
      @Schema(description = "댓글 작성자 닉네임") String userNickname,
      @Schema(description = "댓글 본문") String content,
      @Schema(description = "댓글 좋아요 수(스냅샷)") long likeCount,
      @Schema(description = "댓글 작성 시각(UTC ISO-8601)")
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt
  ) {}

  @Schema(name = "UserActivityDto.CommentLike")
  public static record CommentLike(
      @Schema(description = "좋아요 이벤트 ID") UUID id,
      @Schema(description = "좋아요 시각(UTC ISO-8601)")
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,

      @Schema(description = "대상 댓글 ID") UUID commentId,
      @Schema(description = "대상 댓글의 기사 ID") UUID articleId,
      @Schema(description = "기사 제목") String articleTitle,

      @Schema(description = "댓글 작성자 ID") UUID commentUserId,
      @Schema(description = "댓글 작성자 닉네임") String commentUserNickname,
      @Schema(description = "댓글 본문") String commentContent,

      @Schema(description = "대상 댓글의 총 좋아요 수(스냅샷)") long commentLikeCount,
      @Schema(description = "대상 댓글의 작성 시각(UTC ISO-8601)")
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant commentCreatedAt
  ) {}

  @Schema(name = "UserActivityDto.ArticleView")
  public static record ArticleView(
      @Schema(description = "열람 이벤트 ID") UUID id,
      @Schema(description = "열람자(사용자) ID") UUID viewedBy,
      @Schema(description = "열람 시각(UTC ISO-8601)")
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,
      @Schema(description = "기사 ID") UUID articleId,
      @Schema(description = "수집 출처 코드") String source,
      @Schema(description = "원문 URL") String sourceUrl,
      @Schema(description = "기사 제목") String articleTitle,
      @Schema(description = "기사 원문 발행 시각(UTC ISO-8601)")
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant articlePublishedDate,
      @Schema(description = "기사 요약") String articleSummary,
      @Schema(description = "기사 댓글 수(스냅샷)") long articleCommentCount,
      @Schema(description = "기사 조회 수(스냅샷)") long articleViewCount
  ) {}
}