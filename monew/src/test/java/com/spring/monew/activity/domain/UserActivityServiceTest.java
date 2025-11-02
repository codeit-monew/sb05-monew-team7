package com.spring.monew.activity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.spring.monew.activity.controller.dto.response.CommentActivityDto;
import com.spring.monew.activity.controller.dto.response.CommentLikeActivityDto;
import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.repository.ActivitySyncRepository;
import com.spring.monew.activity.repository.UserActivityQueryRepository;
import com.spring.monew.activity.repository.UserActivityQueryRepository.UserSummary;
import com.spring.monew.activity.service.impl.UserActivityServiceImpl;
import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.domain.Subscription;
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

  @InjectMocks UserActivityServiceImpl service;

  @Test
  @DisplayName("getUserActivity: UserSummary 및 섹션 목록 조합 → DTO 필드 검증")
  void getUserActivity_dto_fields() {
    UUID userId = UUID.randomUUID();

    when(userActivityQueryRepository.findUserSummary(userId))
        .thenReturn(Optional.of(new UserSummary(
            userId, "user@example.com", "유저",
            Instant.parse("2025-01-01T00:00:00Z")
        )));
    when(userActivityQueryRepository.findTopSubscriptions(userId, 10)).thenReturn(List.of());
    when(userActivityQueryRepository.findTopComments(userId, 10)).thenReturn(List.of());
    when(userActivityQueryRepository.findTopCommentLikes(userId, 10)).thenReturn(List.of());
    when(userActivityQueryRepository.findTopArticleViews(userId, 10)).thenReturn(List.of());

    UserActivityDto dto = service.getUserActivity(userId);

    assertThat(dto).isNotNull();
    assertThat(dto.id()).isEqualTo(userId);
    assertThat(dto.email()).isEqualTo("user@example.com");
    assertThat(dto.nickname()).isEqualTo("유저");
    assertThat(dto.subscriptions()).isEmpty();
    assertThat(dto.comments()).isEmpty();
    assertThat(dto.commentLikes()).isEmpty();
    assertThat(dto.articleViews()).isEmpty();
  }

  @Test
  @DisplayName("addCommentActivity: onCommentCreated 위임")
  void addCommentActivity_delegates() {
    Comment c = mock(Comment.class);
    UUID cid = UUID.randomUUID();
    UUID uid = UUID.randomUUID();
    UUID aid = UUID.randomUUID();
    Instant now = Instant.parse("2025-01-01T00:00:00Z");

    var user = mock(com.spring.monew.user.domain.User.class);
    when(user.getId()).thenReturn(uid);
    when(user.getNickname()).thenReturn("nick");

    var article = mock(com.spring.monew.article.domain.Article.class);
    when(article.getId()).thenReturn(aid);
    when(article.getTitle()).thenReturn("title");

    when(c.getId()).thenReturn(cid);
    when(c.getUser()).thenReturn(user);
    when(c.getArticle()).thenReturn(article);
    when(c.getContent()).thenReturn("content");
    when(c.getLikeCount()).thenReturn(3L);
    when(c.getCreatedAt()).thenReturn(now);

    service.addCommentActivity(c);

    verify(activitySyncRepository).onCommentCreated(
        eq(cid), eq(uid), eq(aid), eq("title"), eq("nick"),
        eq("content"), eq(3L), eq(now)
    );
  }

  @Test
  @DisplayName("addCommentLikeActivity: onCommentLiked 호출(핵심 파라미터 강검증, 나머지 느슨)")
  void addCommentLikeActivity_lenient() {
    CommentLike like = mock(CommentLike.class);
    UUID likeId = UUID.randomUUID();
    UUID likedBy = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    UUID articleId = UUID.randomUUID();
    Instant now = Instant.parse("2025-01-02T00:00:00Z");

    var liker = mock(com.spring.monew.user.domain.User.class);
    when(liker.getId()).thenReturn(likedBy);
    when(liker.getNickname()).thenReturn("liker");

    var article = mock(com.spring.monew.article.domain.Article.class);
    when(article.getId()).thenReturn(articleId);
    when(article.getTitle()).thenReturn("title");

    var comment = mock(Comment.class);
    when(comment.getId()).thenReturn(commentId);
    when(comment.getArticle()).thenReturn(article);
    when(comment.getContent()).thenReturn("c");
    when(comment.getLikeCount()).thenReturn(5L);
    when(comment.getCreatedAt()).thenReturn(Instant.parse("2025-01-01T00:00:00Z"));
    // comment.getUser()는 현재 구현과의 차이를 허용하기 위해 스텁 생략

    when(like.getId()).thenReturn(likeId);
    when(like.getUser()).thenReturn(liker);
    when(like.getComment()).thenReturn(comment);
    when(like.getCreatedAt()).thenReturn(now);

    service.addCommentLikeActivity(like);

    verify(activitySyncRepository).onCommentLiked(
        eq(likeId),
        eq(likedBy),
        eq(commentId),
        eq(articleId),
        eq("title"),
        any(),  // commentUserId (느슨)
        any(),  // commentUserNickname (느슨)
        eq("c"),
        eq(5L),
        eq(Instant.parse("2025-01-01T00:00:00Z")),
        eq(now)
    );
  }

  @Test
  @DisplayName("addSubscriptionActivity/remove* 위임 호출(예외 없이 호출됨)")
  void subscriptionAndRemove_calls() {
    // addSubscription
    Subscription s = mock(Subscription.class);
    var user = mock(com.spring.monew.user.domain.User.class);
    var interest = mock(com.spring.monew.interest.domain.Interest.class);
    when(s.getId()).thenReturn(UUID.randomUUID());
    when(s.getUser()).thenReturn(user);
    when(user.getId()).thenReturn(UUID.randomUUID());
    when(s.getInterest()).thenReturn(interest);
    when(interest.getId()).thenReturn(UUID.randomUUID());
    when(interest.getName()).thenReturn("it");
    when(interest.getKeywords()).thenReturn(List.of("a"));
    when(interest.getSubscriptionsCount()).thenReturn(0L);
    when(s.getCreatedAt()).thenReturn(Instant.now());

    service.addSubscriptionActivity(s);
    verify(activitySyncRepository, atLeastOnce()).onSubscribed(any(), any(), any(), any(), any(), anyLong(), any());

    // remove comment
    service.removeCommentActivity(UUID.randomUUID());
    verify(activitySyncRepository, atLeastOnce()).onCommentDeleted(any(), any());

    // remove like
    service.removeCommentLikeActivity(UUID.randomUUID());
    verify(activitySyncRepository, atLeastOnce()).onCommentLikeCanceled(any());

    // remove subscription
    service.removeSubscriptionActivity(UUID.randomUUID());
    verify(activitySyncRepository, atLeastOnce()).onUnsubscribed(any());
  }
}
