package com.spring.monew.activity.repository;

import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//활동 내역 조회 전용 리포지토리(읽기 모델).

public interface UserActivityQueryRepository {

  // RDB에서 사용자 요약 정보만 조회
  record UserSummary(UUID id, String email, String nickname, Instant createdAt) {}

  Optional<UserSummary> findUserSummary(UUID userId);

  List<UserActivityDto.Subscription> findTopSubscriptions(UUID userId, int topN);

  List<UserActivityDto.Comment> findTopComments(UUID userId, int topN);

  List<UserActivityDto.CommentLike> findTopCommentLikes(UUID userId, int topN);

  List<UserActivityDto.ArticleView> findTopArticleViews(UUID userId, int topN);
}