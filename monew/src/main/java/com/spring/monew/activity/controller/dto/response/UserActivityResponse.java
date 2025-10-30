package com.spring.monew.activity.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

// 사용자 활동 스냅샷 응답 DTO.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserActivityResponse(
    // ===== 사용자 식별/기본 정보 =====
    UUID id,                 // 사용자 ID
    String email,            // 사용자 이메일(로그인/표시용)
    String nickname,         // 사용자 닉네임(표시용)

    // 레코드 생성 시각(계정 생성 시각 등) - 항상 UTC 문자열로 직렬화
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,

    // ===== 사용자 활동 요약 컬렉션들 =====
    List<Subscription> subscriptions,   // 사용자가 구독 중인 관심사 목록
    List<Comment> comments,             // 사용자가 작성한 댓글 목록
    List<CommentLike> commentLikes,     // 사용자가 누른 댓글 좋아요 이력
    List<ArticleView> articleViews      // 사용자의 기사 열람 이력
) {

  // 관심사 구독 요약.
  public record Subscription(
      UUID id,                           // 구독 레코드 ID
      UUID interestId,                   // 관심사 ID
      String interestName,               // 관심사 이름(표시용)
      List<String> interestKeywords,     // 관심사 키워드(검색/요약 표시용)
      long interestSubscriberCount,      // 해당 관심사의 전체 구독자 수(스냅샷 지표)
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt // 구독 생성 시각(UTC)
  ) {}

  //댓글 활동 요약.
  public record Comment(
      UUID id,                           // 댓글 ID
      UUID articleId,                    // 대상 기사 ID
      String articleTitle,               // 대상 기사 제목(요약/표시용)
      UUID userId,                       // 댓글 작성자 ID(= 보통 이 응답의 사용자 ID)
      String userNickname,               // 댓글 작성자 닉네임(표시용)
      String content,                    // 댓글 본문
      long likeCount,                    // 댓글 좋아요 수(스냅샷 지표)
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt // 댓글 작성 시각(UTC)
  ) {}

  //댓글 좋아요 활동 요약.
  public record CommentLike(
      UUID id,                           // 좋아요 이벤트 ID(개별 행 식별)
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt, // 좋아요 시각(UTC)

      UUID commentId,                    // 대상 댓글 ID
      UUID articleId,                    // 대상 댓글이 속한 기사 ID
      String articleTitle,               // 기사 제목(요약/표시용)

      UUID commentUserId,                // 해당 댓글 작성자 ID
      String commentUserNickname,        // 해당 댓글 작성자 닉네임
      String commentContent,             // 해당 댓글 본문(요약/표시용)

      long commentLikeCount,             // 해당 댓글의 총 좋아요 수(스냅샷 지표)
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant commentCreatedAt // 해당 댓글의 작성 시각(UTC)
  ) {}

  // 기사 열람 활동 요약.
  public record ArticleView(
      UUID id,                           // 열람 이벤트 ID
      UUID viewedBy,                     // 열람자(사용자) ID
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt, // 열람 시각(UTC)
      UUID articleId,                    // 기사 ID
      String source,                     // 수집 출처 코드(예: NYT, HN 등 사전 정의)
      String sourceUrl,                  // 원문 URL
      String articleTitle,               // 기사 제목
      @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant articlePublishedDate, // 기사 원문 발행 시각(UTC)
      String articleSummary,             // 기사 요약(요약/프리뷰용)
      long articleCommentCount,          // 기사 댓글 수(스냅샷 지표)
      long articleViewCount              // 기사 조회 수(스냅샷 지표)
  ) {}
}