package com.spring.monew.activity.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// PostgreSQL에서 커밋된 이벤트를 기반으로
// MongoDB 조회용 컬렉션(activity_*)을 업서트/삭제하는 동기 Sync 서비스.
public interface ActivitySyncService {

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

  void onUnsubscribed(UUID userId, UUID interestId);

  // ===== 댓글 =====
  void onCommentCreated(
      UUID commentId,
      UUID userId,
      UUID articleId,
      String articleTitleSnapshot,
      String userNicknameSnapshot,
      String content,
      long likeCount,
      Instant createdAt
  );

  void onCommentDeleted(UUID commentId, Instant deletedAt);

  // ===== 댓글 좋아요 =====
  void onCommentLiked(
      UUID likeEventId,
      UUID likedByUserId,
      UUID commentId,
      UUID articleId,
      String articleTitleSnapshot,          // 추가
      UUID commentUserId,
      String commentUserNicknameSnapshot,
      String commentContentSnapshot,
      long commentLikeCountSnapshot,
      Instant commentCreatedAtSnapshot,
      Instant likedAt
  );

  void onCommentLikeCanceled(UUID likeEventId);

  // ===== 기사 조회 =====
  void onArticleViewed(
      UUID viewEventId,
      UUID userId,
      UUID articleId,
      String source,
      String sourceUrl,
      String articleTitleSnapshot,
      Instant articlePublishDateSnapshot,
      String articleSummarySnapshot,
      long articleCommentCountSnapshot,
      long articleViewCountSnapshot,
      Instant viewedAt
  );
}