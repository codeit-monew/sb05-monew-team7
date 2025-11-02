package com.spring.monew.activity.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.spring.monew.activity.controller.dto.response.CommentActivityDto;
import com.spring.monew.activity.controller.dto.response.CommentLikeActivityDto;
import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import com.spring.monew.activity.repository.impl.UserActivityQueryRepositoryImpl;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
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
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@ExtendWith(MockitoExtension.class)
class UserActivityRepositoryTest {

  @Mock MongoTemplate mongo;
  @Mock JdbcTemplate jdbc;
  @InjectMocks UserActivityQueryRepositoryImpl sut;

  @Test
  @DisplayName("findUserSummary: 존재하면 Optional.present")
  void findUserSummary_present() {
    UUID uid = UUID.randomUUID();
    when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(uid)))
        .thenReturn(new UserActivityQueryRepository.UserSummary(
            uid, "user@example.com", "유저", Instant.parse("2025-01-01T00:00:00Z")
        ));

    Optional<UserActivityQueryRepository.UserSummary> opt = sut.findUserSummary(uid);
    assertThat(opt).isPresent();
    assertThat(opt.get().id()).isEqualTo(uid);
    assertThat(opt.get().email()).isEqualTo("user@example.com");
    assertThat(opt.get().nickname()).isEqualTo("유저");
  }

  @Test
  @DisplayName("findUserSummary: 없으면 Optional.empty")
  void findUserSummary_empty() {
    UUID uid = UUID.randomUUID();
    when(jdbc.queryForObject(anyString(), any(RowMapper.class), eq(uid)))
        .thenThrow(new EmptyResultDataAccessException(1));
    assertThat(sut.findUserSummary(uid)).isEmpty();
  }

  @Test
  @DisplayName("findTopComments: 결과 없으면 빈 리스트")
  void findTopComments_empty() {
    UUID uid = UUID.randomUUID();
    when(mongo.find(any(Query.class), eq(ActivityCommentDoc.class))).thenReturn(List.of());
    List<CommentActivityDto> list = sut.findTopComments(uid, 5);
    assertThat(list).isEmpty();
  }

  @Test
  @DisplayName("findTopComments: 스냅샷 닉네임이 없으면 RDB 폴백으로 채움")
  void findTopComments_withFallback() {
    UUID uid = UUID.randomUUID();
    ActivityCommentDoc d = new ActivityCommentDoc();
    d.setId(UUID.randomUUID().toString());
    d.setUserId(uid);
    d.setArticleId(UUID.randomUUID());
    d.setArticleTitle("제목");
    d.setContent("내용");
    d.setLikeCount(0);
    d.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
    d.setUserNickname(null);

    when(mongo.find(any(Query.class), eq(ActivityCommentDoc.class))).thenReturn(List.of(d));
    when(jdbc.queryForObject(anyString(), eq(String.class), eq(uid))).thenReturn("폴백닉");

    List<CommentActivityDto> list = sut.findTopComments(uid, 5);
    assertThat(list).hasSize(1);
    assertThat(list.get(0).userNickname()).isEqualTo("폴백닉");
    assertThat(list.get(0).content()).isEqualTo("내용");
  }

  @Test
  @DisplayName("findTopCommentLikes: 결과 없으면 빈 리스트")
  void findTopCommentLikes_empty() {
    UUID uid = UUID.randomUUID();
    when(mongo.find(any(Query.class), eq(ActivityCommentLikeDoc.class))).thenReturn(List.of());
    List<CommentLikeActivityDto> list = sut.findTopCommentLikes(uid, 5);
    assertThat(list).isEmpty();
  }

  @Test
  @DisplayName("findTopCommentLikes: 필드 매핑 검증")
  void findTopCommentLikes_fields() {
    UUID uid = UUID.randomUUID();
    ActivityCommentLikeDoc d = new ActivityCommentLikeDoc();
    d.setId(UUID.randomUUID().toString());
    d.setUserId(uid);
    d.setCommentId(UUID.randomUUID());
    d.setArticleId(UUID.randomUUID());
    d.setArticleTitle("제목");
    d.setCommentUserId(UUID.randomUUID());
    d.setCommentUserNickname("작성자");
    d.setCommentContent("댓");
    d.setCommentLikeCount(7);
    d.setCommentCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
    d.setCreatedAt(Instant.parse("2025-01-02T00:00:00Z"));

    when(mongo.find(any(Query.class), eq(ActivityCommentLikeDoc.class))).thenReturn(List.of(d));

    List<CommentLikeActivityDto> list = sut.findTopCommentLikes(uid, 5);
    assertThat(list).hasSize(1);
    assertThat(list.get(0).commentContent()).isEqualTo("댓");
    assertThat(list.get(0).commentLikeCount()).isEqualTo(7);
    assertThat(list.get(0).commentUserNickname()).isEqualTo("작성자");
  }

  @Test
  @DisplayName("findTopSubscriptions: 필드 매핑 검증 (record DTO)")
  void findTopSubscriptions_fields() {
    UUID uid = UUID.randomUUID();
    UUID interestId = UUID.randomUUID();
    UserInterestSubscriptionDoc d = new UserInterestSubscriptionDoc(
        UUID.randomUUID().toString(),
        uid,
        interestId,
        "AI",
        List.of("인공지능", "머신러닝"),
        123L,
        Instant.parse("2025-01-01T00:00:00Z")
    );
    when(mongo.find(any(Query.class), eq(UserInterestSubscriptionDoc.class))).thenReturn(List.of(d));

    List<SubscriptionDto> list = sut.findTopSubscriptions(uid, 3);
    assertThat(list).hasSize(1);

    SubscriptionDto dto = list.get(0);
    assertThat(dto.id()).isNotNull();
    assertThat(dto.interestId()).isEqualTo(interestId);
    assertThat(dto.interestName()).isEqualTo("AI");
    assertThat(dto.interestKeywords()).containsExactly("인공지능", "머신러닝");
    assertThat(dto.interestSubscriberCount()).isEqualTo(123L);
    assertThat(dto.createdAt()).isNotNull();
  }

  @Test
  @DisplayName("findTopArticleViews: 필드 매핑 검증 (record DTO)")
  void findTopArticleViews_fields() {
    UUID uid = UUID.randomUUID();
    UUID articleId = UUID.randomUUID();
    ActivityArticleViewDoc d = new ActivityArticleViewDoc();
    d.setId(UUID.randomUUID().toString());
    d.setUserId(uid);
    d.setArticleId(articleId);
    d.setSource(ArticleSource.CHOSUN);
    d.setSourceUrl("https://news.example.com/1");
    d.setTitle("제목");
    d.setSummary("요약");
    d.setCommentCount(0L);
    d.setViewCount(10L);
    d.setPublishDate(Instant.parse("2025-01-01T00:00:00Z"));
    d.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
    d.setLastViewedAt(Instant.parse("2025-01-02T00:00:00Z"));

    when(mongo.find(any(Query.class), eq(ActivityArticleViewDoc.class))).thenReturn(List.of(d));

    List<ArticleViewDto> list = sut.findTopArticleViews(uid, 4);
    assertThat(list).hasSize(1);

    ArticleViewDto dto = list.get(0);
    assertThat(dto.articleId()).isEqualTo(articleId);
    assertThat(dto.articleTitle()).isEqualTo("제목");
    assertThat(dto.source()).isEqualTo(ArticleSource.CHOSUN);
    assertThat(dto.viewedBy()).isEqualTo(uid);
    assertThat(dto.createdAt()).isNotNull();
    assertThat(dto.sourceUrl()).isEqualTo("https://news.example.com/1");
    assertThat(dto.articleSummary()).isEqualTo("요약");
    assertThat(dto.articleCommentCount()).isEqualTo(0L);
    assertThat(dto.articleViewCount()).isEqualTo(10L);
  }
}
