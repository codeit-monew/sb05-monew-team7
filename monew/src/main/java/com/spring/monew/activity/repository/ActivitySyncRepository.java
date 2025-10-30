package com.spring.monew.activity.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

//RDB 트랜잭션 커밋 이후(AFTER_COMMIT) Activity 스냅샷을 MongoDB에 동기화하기 위한 포트.
//UUID/String 혼용 매칭은 구현체에서 처리한다.

public interface ActivitySyncRepository {

  // ===== 구독 =====
  void onSubscribed(
      UUID subscriptionId,
      UUID userId,
      UUID interestId,
      String interestName,
      List<String> interestKeywords,
      long interestSubscriberCount,
      Instant createdAt
  );

  // subscriptionId 기준 단건 삭제
  void onUnsubscribed(UUID subscriptionId);

  // (userId, interestId) 복합키 기준 삭제
  void onUnsubscribed(UUID userId, UUID interestId);

  // ===== 댓글 =====
  void onCommentCreated(
      UUID commentId,
      UUID userId,
      UUID articleId,
      String articleTitle,
      String commentUserNickname,
      String content,
      long likeCount,
      Instant createdAt
  );

  void onCommentDeleted(UUID commentId);

  // 삭제 시각은 현재 구현에서 사용하지 않지만 시그니처 유지
  void onCommentDeleted(UUID commentId, Instant deletedAt);

  // ===== 댓글 좋아요 =====
  void onCommentLiked(
      UUID likeEventId,
      UUID userId,                // likedBy
      UUID commentId,
      UUID articleId,
      String articleTitle,
      UUID commentUserId,
      String commentUserNickname,
      String commentContent,
      long commentLikeCount,
      Instant commentCreatedAt,
      Instant likeCreatedAt
  );

  void onCommentLikeCanceled(UUID likeId);

  // ===== 기사 열람(최근 본 기사) =====
  void onArticleViewed(
      UUID viewEventId,
      UUID userId,
      UUID articleId,
      String source,
      String sourceUrl,
      String articleTitle,
      Instant articlePublishedDate,
      String articleSummary,
      Long articleCommentCount,
      Long articleViewCount,
      Instant createdAt
  );
}