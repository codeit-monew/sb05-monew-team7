package com.spring.monew.activity.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import com.spring.monew.activity.repository.impl.UserActivityQueryRepositoryImpl;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import java.sql.ResultSet;
import java.sql.Timestamp;
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

@ExtendWith(MockitoExtension.class)
class UserActivityRepositoryTest {

  @Mock MongoTemplate mongo;
  @Mock JdbcTemplate jdbc;

  @InjectMocks UserActivityQueryRepositoryImpl sut;

  @Test
  @DisplayName("findUserSummary: 1건 반환")
  void find_user_summary_ok() throws Exception {
    UUID uid = UUID.randomUUID();

    when(jdbc.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(uid)))
        .thenAnswer(inv -> {
          var rm = inv.getArgument(1, org.springframework.jdbc.core.RowMapper.class);
          ResultSet rs = org.mockito.Mockito.mock(ResultSet.class);
          when(rs.getObject("id")).thenReturn(uid);
          when(rs.getString("email")).thenReturn("user@example.com");
          when(rs.getString("nickname")).thenReturn("유저");
          when(rs.getTimestamp("created_at")).thenReturn(Timestamp.from(Instant.parse("2025-01-01T00:00:00Z")));
          return rm.mapRow(rs, 0);
        });

    Optional<UserActivityQueryRepository.UserSummary> opt = sut.findUserSummary(uid);

    assertThat(opt).isPresent();
    assertThat(opt.get().id()).isEqualTo(uid);
    assertThat(opt.get().email()).isEqualTo("user@example.com");
    assertThat(opt.get().nickname()).isEqualTo("유저");
  }

  @Test
  @DisplayName("findUserSummary: 결과 없음 → Optional.empty()")
  void find_user_summary_empty() {
    UUID uid = UUID.randomUUID();
    when(jdbc.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq(uid)))
        .thenThrow(new EmptyResultDataAccessException(1));

    Optional<UserActivityQueryRepository.UserSummary> opt = sut.findUserSummary(uid);
    assertThat(opt).isEmpty();
  }

  @Test
  @DisplayName("findTopComments: Mongo에서 문서 조회하여 DTO로 매핑")
  void find_top_comments() {
    UUID uid = UUID.randomUUID();
    ActivityCommentDoc doc = new ActivityCommentDoc(
        UUID.randomUUID().toString(),
        uid,
        UUID.randomUUID(),
        "기사제목",
        "댓글내용",
        7L,
        Instant.parse("2025-01-03T00:00:00Z"),
        "닉"
    );
    when(mongo.find(any(Query.class), eq(ActivityCommentDoc.class)))
        .thenReturn(List.of(doc));

    var list = sut.findTopComments(uid, 5);

    assertThat(list).hasSize(1);
    assertThat(list.get(0).userNickname()).isEqualTo("닉");
    assertThat(list.get(0).content()).isEqualTo("댓글내용");
    assertThat(list.get(0).articleTitle()).isEqualTo("기사제목");
  }

  @Test
  @DisplayName("findTopCommentLikes: Mongo에서 문서 조회하여 DTO로 매핑")
  void find_top_comment_likes() {
    UUID uid = UUID.randomUUID();
    ActivityCommentLikeDoc doc = new ActivityCommentLikeDoc(
        UUID.randomUUID().toString(),
        uid,
        UUID.randomUUID(),
        UUID.randomUUID(),
        "기사제목",
        UUID.randomUUID(),
        "작성자닉",
        "댓글내용",
        10L,
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2025-01-04T00:00:00Z")
    );
    when(mongo.find(any(Query.class), eq(ActivityCommentLikeDoc.class)))
        .thenReturn(List.of(doc));

    var list = sut.findTopCommentLikes(uid, 5);

    assertThat(list).hasSize(1);
    assertThat(list.get(0).commentUserNickname()).isEqualTo("작성자닉");
    assertThat(list.get(0).commentLikeCount()).isEqualTo(10L);
  }

  @Test
  @DisplayName("findTopSubscriptions: Mongo에서 문서 조회 → DTO 목록 반환")
  void find_top_subscriptions() {
    UUID uid = UUID.randomUUID();
    UserInterestSubscriptionDoc doc = new UserInterestSubscriptionDoc(
        UUID.randomUUID().toString(),
        uid,
        UUID.randomUUID(),
        "AI",
        List.of("생성형"),
        123L,
        Instant.parse("2025-01-02T00:00:00Z")
    );
    when(mongo.find(any(Query.class), eq(UserInterestSubscriptionDoc.class)))
        .thenReturn(List.of(doc));

    List<SubscriptionDto> list = sut.findTopSubscriptions(uid, 3);
    // SubscriptionDto의 필드명은 외부 모듈에 의존 → 크기만 검증
    assertThat(list).hasSize(1);
  }

  @Test
  @DisplayName("findTopArticleViews: Mongo에서 문서 조회 → 목록 반환")
  void find_top_article_views() {
    UUID uid = UUID.randomUUID();
    ActivityArticleViewDoc doc = new ActivityArticleViewDoc(
        UUID.randomUUID().toString(),
        uid,
        UUID.randomUUID(),
        ArticleSource.CHOSUN,
        "https://example.com",
        "제목",
        "요약",
        5L,
        100L,
        Instant.parse("2024-12-31T00:00:00Z"),
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2025-01-05T00:00:00Z")
    );
    when(mongo.find(any(Query.class), eq(ActivityArticleViewDoc.class)))
        .thenReturn(List.of(doc));

    var list = sut.findTopArticleViews(uid, 4);
    assertThat(list).hasSize(1);
  }
}
