package com.spring.monew.activity.service.impl;

import com.spring.monew.activity.controller.dto.response.CommentActivityDto;
import com.spring.monew.activity.controller.dto.response.CommentLikeActivityDto;
import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.repository.ActivitySyncRepository;
import com.spring.monew.activity.repository.UserActivityQueryRepository;
import com.spring.monew.activity.repository.UserActivityQueryRepository.UserSummary;
import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.domain.Subscription;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserActivityServiceImpl implements UserActivityService {

  private static final int TOP_N = 10;
  private final ActivitySyncRepository activitySyncRepository;
  private final UserActivityQueryRepository userActivityQueryRepository;

  @Override
  @Transactional(readOnly = true)
  public UserActivityDto getUserActivity(UUID userId) {
    UserSummary u = userActivityQueryRepository.findUserSummary(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    List<SubscriptionDto> subs = userActivityQueryRepository.findTopSubscriptions(userId, TOP_N);
    List<CommentActivityDto> comments = userActivityQueryRepository.findTopComments(userId, TOP_N);
    List<CommentLikeActivityDto> likes = userActivityQueryRepository.findTopCommentLikes(userId,
        TOP_N);
    List<ArticleViewDto> views = userActivityQueryRepository.findTopArticleViews(userId, TOP_N);

    return new UserActivityDto(
        u.id(), u.email(), u.nickname(), u.createdAt(),
        subs, comments, likes, views
    );
  }

  @Override
  @Transactional
  public void addCommentActivity(Comment comment) {
    try {
      activitySyncRepository.onCommentCreated(
          comment.getId(),
          comment.getUser().getId(),
          comment.getArticle().getId(),
          comment.getArticle().getTitle(),
          comment.getUser().getNickname(),
          comment.getContent(),
          comment.getLikeCount(),
          comment.getCreatedAt()
      );
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (댓글 수정→스냅샷 갱신): commentId={}, userId={}, articleId={}",
          comment.getId(),
          comment.getUser().getId(),
          comment.getArticle().getId(), e);
    }
  }

  @Override
  @Transactional
  public void addCommentLikeActivity(CommentLike commentLike) {
    try {
      activitySyncRepository.onCommentLiked(
          commentLike.getId(),
          commentLike.getUser().getId(),
          commentLike.getComment().getId(),
          commentLike.getComment().getArticle().getId(),
          commentLike.getComment().getArticle().getTitle(),
          commentLike.getUser().getId(),
          commentLike.getUser().getNickname(),
          commentLike.getComment().getContent(),
          commentLike.getComment().getLikeCount(),
          commentLike.getComment().getCreatedAt(),
          commentLike.getCreatedAt()
      );
    } catch (Exception e) {
      log.warn(
          "활동 동기화 실패 (댓글 좋아요 생성 afterCommit): likeId={}, commentId={}, likedByUserId={}, articleId={}",
          commentLike.getId(),
          commentLike.getUser().getId(),
          commentLike.getComment().getId(),
          commentLike.getComment().getArticle().getId(), e);
    }
  }

  @Override
  @Transactional
  public void addSubscriptionActivity(Subscription subscription) {
    try {
      log.info("관심사 구독 함.");
      activitySyncRepository.onSubscribed(
          subscription.getId(),
          subscription.getUser().getId(),
          subscription.getInterest().getId(),
          subscription.getInterest().getName(),
          subscription.getInterest().getKeywords(),
          subscription.getInterest().getSubscriptionsCount(),
          subscription.getCreatedAt()
      );
    } catch (Exception e) {
      // 실패 시 본 기능은 유지하고 경고 로그 + 스택트레이스 남김
      log.warn("활동 동기화 실패 (구독 생성 afterCommit): subscriptionId={}, userId={}, interestId={}",
          subscription.getId(),
          subscription.getUser().getId(),
          subscription.getInterest().getId(), e);
    }
  }

  @Override
  @Transactional
  public void removeCommentActivity(UUID commentId) {
    try {
      activitySyncRepository.onCommentDeleted(commentId, Instant.now());
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (댓글 물리 삭제): commentId={}", commentId, e);
    }
  }

  @Override
  @Transactional
  public void removeCommentLikeActivity(UUID commentLikeId) {
    try {
      activitySyncRepository.onCommentLikeCanceled(commentLikeId);
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (댓글 좋아요 취소 afterCommit): likeId={}",
          commentLikeId, e);
    }
  }

  @Override
  @Transactional
  public void removeSubscriptionActivity(UUID subscriptionId) {
    try {
      activitySyncRepository.onUnsubscribed(subscriptionId);
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (구독 해제 afterCommit): subscriptionId={}", subscriptionId, e);
    }
  }
}