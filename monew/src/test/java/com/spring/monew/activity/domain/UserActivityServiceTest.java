package com.spring.monew.activity.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.isNull;

import com.spring.monew.activity.controller.dto.response.CommentActivityDto;
import com.spring.monew.activity.controller.dto.response.CommentLikeActivityDto;
import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.repository.ActivitySyncRepository;
import com.spring.monew.activity.repository.UserActivityQueryRepository;
import com.spring.monew.activity.repository.UserActivityQueryRepository.UserSummary;
import com.spring.monew.activity.service.impl.UserActivityServiceImpl;
import com.spring.monew.article.domain.Article;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.domain.Subscription;
import com.spring.monew.user.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserActivityServiceTest {

  @Mock ActivitySyncRepository activitySyncRepository;
  @Mock UserActivityQueryRepository userActivityQueryRepository;

  @InjectMocks UserActivityServiceImpl userActivityService;

  UUID userId = UUID.randomUUID();

  @Test
  @DisplayName("활동 요약 조회 성공 - 요약/상위 N 목록을 합쳐 DTO 반환")
  void getUserActivity_success() {
    when(userActivityQueryRepository.findUserSummary(userId))
        .thenReturn(Optional.of(new UserSummary(
            userId, "user@example.com", "유저", Instant.parse("2025-01-01T00:00:00Z")
        )));

    when(userActivityQueryRepository.findTopSubscriptions(eq(userId), anyInt()))
        .thenReturn(List.of(new SubscriptionDto(
            UUID.randomUUID(), UUID.randomUUID(), "AI", List.of("생성형"), 123L,
            Instant.parse("2025-01-02T00:00:00Z")
        )));

    when(userActivityQueryRepository.findTopComments(eq(userId), anyInt()))
        .thenReturn(List.of(new CommentActivityDto(
            UUID.randomUUID(), UUID.randomUUID(), "기사제목",
            userId, "유저", "댓글내용", 3L,
            Instant.parse("2025-01-03T00:00:00Z")
        )));

    when(userActivityQueryRepository.findTopCommentLikes(eq(userId), anyInt()))
        .thenReturn(List.of(new CommentLikeActivityDto(
            UUID.randomUUID(),
            Instant.parse("2025-01-04T00:00:00Z"),
            UUID.randomUUID(), UUID.randomUUID(), "기사제목",
            UUID.randomUUID(), "작성자닉", "댓글내용", 10L,
            Instant.parse("2025-01-01T00:00:00Z")
        )));

    // ArticleViewDto 시그니처 의존 제거 → 빈 리스트로 스텁
    when(userActivityQueryRepository.findTopArticleViews(eq(userId), anyInt()))
        .thenReturn(List.of());

    // 실행 (예외 없이 DTO 생성되면 성공)
    UserActivityDto dto = userActivityService.getUserActivity(userId);
  }

  @Test
  @DisplayName("활동 요약 조회 실패 - 사용자 없음")
  void getUserActivity_userNotFound() {
    when(userActivityQueryRepository.findUserSummary(userId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> userActivityService.getUserActivity(userId))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("댓글 생성 이벤트 → SyncRepository 위임")
  void addCommentActivity_delegate() {
    UUID cId = UUID.randomUUID();
    UUID uId = UUID.randomUUID();
    UUID aId = UUID.randomUUID();

    Comment comment = mock(Comment.class);
    User user = mock(User.class);
    Article article = mock(Article.class);

    when(comment.getId()).thenReturn(cId);
    when(comment.getUser()).thenReturn(user);
    when(comment.getArticle()).thenReturn(article);
    when(comment.getContent()).thenReturn("내용");
    when(comment.getLikeCount()).thenReturn(3L);
    when(comment.getCreatedAt()).thenReturn(Instant.parse("2025-01-03T00:00:00Z"));
    when(user.getId()).thenReturn(uId);
    when(user.getNickname()).thenReturn("유저");
    when(article.getId()).thenReturn(aId);
    when(article.getTitle()).thenReturn("기사제목");

    userActivityService.addCommentActivity(comment);

    verify(activitySyncRepository).onCommentCreated(
        eq(cId), eq(uId), eq(aId), eq("기사제목"), eq("유저"),
        eq("내용"), eq(3L), eq(Instant.parse("2025-01-03T00:00:00Z"))
    );
  }

  @Test
  @DisplayName("댓글 좋아요 생성 이벤트 → SyncRepository 위임 (구현과 동일하게 likedBy/닉네임 null)")
  void addCommentLikeActivity_delegate() {
    UUID likeId = UUID.randomUUID();
    UUID likedBy = UUID.randomUUID();     // 좋아요 한 사용자
    UUID commentId = UUID.randomUUID();
    UUID articleId = UUID.randomUUID();

    CommentLike like = mock(CommentLike.class);
    Comment comment = mock(Comment.class);
    Article article = mock(Article.class);
    User liker = mock(User.class);

    when(like.getId()).thenReturn(likeId);
    when(like.getUser()).thenReturn(liker);
    when(liker.getId()).thenReturn(likedBy);
    when(like.getCreatedAt()).thenReturn(Instant.parse("2025-01-04T00:00:00Z"));

    when(like.getComment()).thenReturn(comment);
    when(comment.getId()).thenReturn(commentId);
    when(comment.getArticle()).thenReturn(article);
    when(comment.getContent()).thenReturn("댓글내용");
    when(comment.getLikeCount()).thenReturn(10L);
    when(comment.getCreatedAt()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));

    when(article.getId()).thenReturn(articleId);
    when(article.getTitle()).thenReturn("기사제목");

    // 실행
    userActivityService.addCommentLikeActivity(like);

    // 현재 구현과 동일: commentUserId == likedBy, commentUserNickname == null
    verify(activitySyncRepository).onCommentLiked(
        eq(likeId),
        eq(likedBy),
        eq(commentId),
        eq(articleId),
        eq("기사제목"),
        eq(likedBy),      // commentUserId
        isNull(),         // commentUserNickname
        eq("댓글내용"),
        eq(10L),
        eq(Instant.parse("2025-01-01T00:00:00Z")),
        eq(Instant.parse("2025-01-04T00:00:00Z"))
    );
  }

  @Test
  @DisplayName("댓글/좋아요/구독 삭제 이벤트 → SyncRepository 위임")
  void remove_events_delegate() {
    UUID cid = UUID.randomUUID();
    UUID lid = UUID.randomUUID();
    UUID sid = UUID.randomUUID();

    userActivityService.removeCommentActivity(cid);
    verify(activitySyncRepository).onCommentDeleted(eq(cid), any(Instant.class));

    userActivityService.removeCommentLikeActivity(lid);
    verify(activitySyncRepository).onCommentLikeCanceled(eq(lid));

    userActivityService.removeSubscriptionActivity(sid);
    verify(activitySyncRepository).onUnsubscribed(eq(sid));
  }

  @Test
  @DisplayName("구독 생성 이벤트 → SyncRepository 위임")
  void addSubscriptionActivity_delegate() {
    UUID subId = UUID.randomUUID();
    UUID uId = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();

    Subscription sub = mock(Subscription.class);
    User user = mock(User.class);
    com.spring.monew.interest.domain.Interest interest =
        mock(com.spring.monew.interest.domain.Interest.class);

    when(sub.getId()).thenReturn(subId);
    when(sub.getUser()).thenReturn(user);
    when(sub.getCreatedAt()).thenReturn(Instant.parse("2025-01-02T00:00:00Z"));
    when(user.getId()).thenReturn(uId);

    when(sub.getInterest()).thenReturn(interest);
    when(interest.getId()).thenReturn(interestId);
    when(interest.getName()).thenReturn("AI");
    when(interest.getKeywords()).thenReturn(List.of("생성형"));
    // ✨ 구현과 동일: getSubscriptionsCount()
    when(interest.getSubscriptionsCount()).thenReturn(123L);

    userActivityService.addSubscriptionActivity(sub);

    verify(activitySyncRepository).onSubscribed(
        eq(subId),
        eq(uId),
        eq(interestId),
        eq("AI"),
        eq(List.of("생성형")),
        eq(123L),
        eq(Instant.parse("2025-01-02T00:00:00Z"))
    );
  }
}
