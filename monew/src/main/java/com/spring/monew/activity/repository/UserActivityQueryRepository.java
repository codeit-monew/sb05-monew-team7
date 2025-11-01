package com.spring.monew.activity.repository;

import com.spring.monew.activity.controller.dto.response.CommentActivityDto;
import com.spring.monew.activity.controller.dto.response.CommentLikeActivityDto;
import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//활동 내역 조회 전용 리포지토리(읽기 모델).

public interface UserActivityQueryRepository {

  // RDB에서 사용자 요약 정보만 조회
  record UserSummary(UUID id, String email, String nickname, Instant createdAt) {}

  Optional<UserSummary> findUserSummary(UUID userId);

  List<SubscriptionDto> findTopSubscriptions(UUID userId, int topN);

  List<CommentActivityDto> findTopComments(UUID userId, int topN);

  List<CommentLikeActivityDto> findTopCommentLikes(UUID userId, int topN);

  List<ArticleViewDto> findTopArticleViews(UUID userId, int topN);
}