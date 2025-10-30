package com.spring.monew.activity.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// PostgreSQL에서 커밋된 도메인 이벤트를 기반으로
// MongoDB 조회용 컬렉션(activity_*)을 업서트/삭제하는 "쓰기 전용" Repository.

public interface ActivitySyncRepository {

  // ===== 구독 =====
  void onSubscribed(
      UUID subscriptionId,               // _id 로 setOnInsert. 복합키(q)로 매칭
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
      UUID commentId,                    // _id = commentId
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
      UUID likeEventId,                  // _id = likeEventId (이벤트 로그)
      UUID likedByUserId,
      UUID commentId,
      UUID articleId,
      String articleTitleSnapshot,
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
      UUID viewEventId,                  // _id setOnInsert. (user_id, article_id) 업서트 기준
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