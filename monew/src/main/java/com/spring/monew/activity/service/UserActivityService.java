package com.spring.monew.activity.service;

import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import com.spring.monew.activity.repository.UserActivityQueryRepository;
import com.spring.monew.activity.repository.UserActivityQueryRepository.CursorKey;
import com.spring.monew.activity.repository.UserActivityQueryRepository.RepoSlice;
import com.spring.monew.activity.repository.UserActivityQueryRepository.UserSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserActivityService {

  private final UserActivityQueryRepository repo;

  @Transactional(readOnly = true)
  public UserActivityDto getUserActivity(UUID userId, int limit, String cursorStr) {
    int pageSize = clamp(limit);
    CursorKey cursor = decodeCursor(cursorStr);

    // 1) 사용자 요약(RDB)
    UserSummary user = repo.fetchUserSummary(userId);

    // 2) 섹션 조회(Repo → Doc 반환)
    RepoSlice<UserInterestSubscriptionDoc> subs    = repo.findRecentSubscriptions(userId, pageSize);
    RepoSlice<ActivityCommentDoc>          comments = repo.findComments(userId, pageSize, cursor);
    RepoSlice<ActivityCommentLikeDoc>      likes    = repo.findCommentLikes(userId, pageSize, cursor);
    RepoSlice<ActivityArticleViewDoc>      views    = repo.findArticleViews(userId, pageSize, cursor);

    // 3) DTO 매핑
    List<UserActivityDto.Subscription> subDtos = subs.items().stream()
        .map(d -> new UserActivityDto.Subscription(
            d.getId(),
            d.getInterestId(),
            d.getInterestName(),
            d.getInterestKeywords(),
            d.getInterestSubscriberCount(),
            d.getCreatedAt()
        ))
        .toList();

    List<UserActivityDto.Comment> commentDtos = comments.items().stream()
        .map(d -> new UserActivityDto.Comment(
            d.getId(),
            d.getArticleId(),
            nz(d.getArticleTitle()),
            d.getUserId(),
            nz(d.getUserNickname()),
            nz(d.getContent()),
            d.getLikeCount(),
            d.getCreatedAt()
        ))
        .toList();

    List<UserActivityDto.CommentLike> likeDtos = likes.items().stream()
        .map(d -> new UserActivityDto.CommentLike(
            d.getId(),
            // 표시 우선순위: 이벤트 시각(없으면 댓글 작성 시각)
            pickInstant(d.getCreatedAt(), d.getCommentCreatedAt()),
            d.getCommentId(),
            d.getArticleId(),
            nz(d.getArticleTitle()),
            d.getCommentUserId(),
            nz(d.getCommentUserNickname()),
            nz(d.getCommentContent()),
            d.getCommentLikeCount(),
            d.getCommentCreatedAt()
        ))
        // Repo에서 이미 정렬되어 오지만, UI 전용 기준을 강제하고 싶다면 남겨둠
        .sorted(
            java.util.Comparator
                .comparing(UserActivityDto.CommentLike::createdAt,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                .thenComparing(UserActivityDto.CommentLike::id)
                .reversed()
        )
        .toList();

    List<UserActivityDto.ArticleView> viewDtos = views.items().stream()
        .map(d -> new UserActivityDto.ArticleView(
            d.getId(),
            userId,
            d.getLastViewedAt(),
            d.getArticleId(),
            d.getSource(),
            d.getSourceUrl(),
            nz(d.getArticleTitle()),
            d.getArticlePublishedDate(),
            nz(d.getArticleSummary()),
            d.getArticleCommentCount(),
            d.getArticleViewCount()
        ))
        .toList();

    // 4) 커서(next) 생성: createdAt|id → Base64URL
    String next = encodeCursor(comments.lastKey());

    return new UserActivityDto(
        user.id(), user.email(), user.nickname(), user.createdAt(),
        subDtos, commentDtos, likeDtos, viewDtos
    );
  }

  // ===== util =====
  private static int clamp(int limit) {
    return Math.max(1, Math.min(limit, 100));
  }

  private static String nz(String v) {
    return v == null ? "" : v;
  }

  private static Instant pickInstant(Instant... cands) {
    for (Instant i : cands) if (i != null) return i;
    return null;
  }

  private CursorKey decodeCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    try {
      String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
      String[] parts = raw.split("\\|");
      if (parts.length != 2) return null;
      return new CursorKey(
          Instant.ofEpochMilli(Long.parseLong(parts[0])),
          UUID.fromString(parts[1])
      );
    } catch (Exception e) {
      return null; // 손상 커서는 초기 페이지로
    }
  }

  private String encodeCursor(CursorKey key) {
    if (key == null) return null;
    String raw = key.createdAt().toEpochMilli() + "|" + key.id();
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }
}