package com.spring.monew.activity.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

  void onUnsubscribed(UUID subscriptionId);
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

  // 호환용 1-파라미터 → 2-파라미터로 위임
  default void onCommentLikeCanceled(UUID likeEventId) {
    onCommentLikeCanceled(likeEventId, null);
  }

  // 실제 구현해야 할 시그니처(필요시 userId 전달)
  void onCommentLikeCanceled(UUID likeEventId, UUID userId);
}