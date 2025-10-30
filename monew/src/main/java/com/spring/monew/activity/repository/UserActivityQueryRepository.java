package com.spring.monew.activity.repository;

import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface UserActivityQueryRepository {

  // RDB 사용자 요약 (표시용 최소 필드)
  UserSummary fetchUserSummary(UUID userId);

  // 최신 구독 N (커서 없음)
  RepoSlice<UserInterestSubscriptionDoc> findRecentSubscriptions(UUID userId, int limit);

  // 커서 기반 섹션들
  RepoSlice<ActivityCommentDoc> findComments(UUID userId, int limit, CursorKey cursor);
  RepoSlice<ActivityCommentLikeDoc> findCommentLikes(UUID userId, int limit, CursorKey cursor);
  RepoSlice<ActivityArticleViewDoc> findArticleViews(UUID userId, int limit, CursorKey cursor);

  // ===== 공용 모델 =====
  record UserSummary(UUID id, String email, String nickname, Instant createdAt) {}
  record CursorKey(Instant createdAt, UUID id) {}
  record RepoSlice<T>(List<T> items, boolean hasMore, CursorKey lastKey) {}
}