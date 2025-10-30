package com.spring.monew.activity.repository.impl;

import static org.springframework.data.domain.Sort.Order.desc;

import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import com.spring.monew.activity.repository.UserActivityQueryRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserActivityQueryRepositoryImpl implements UserActivityQueryRepository {

  private final MongoTemplate mongo;
  private final JdbcTemplate jdbc;

  // ===== RDB: 사용자 요약 =====
  @Override
  public UserSummary fetchUserSummary(UUID userId) {
    try {
      return jdbc.queryForObject(
          """
          select id, email, nickname, created_at
          from users
          where id = ?
          """,
          (rs, rn) -> new UserSummary(
              (UUID) rs.getObject("id"),
              rs.getString("email"),
              rs.getString("nickname"),
              toInstant(rs.getObject("created_at"))
          ),
          userId
      );
    } catch (EmptyResultDataAccessException e) {
      throw new NoSuchElementException("User not found: " + userId);
    }
  }

  // ===== Mongo: 구독 N =====
  @Override
  public RepoSlice<UserInterestSubscriptionDoc> findRecentSubscriptions(UUID userId, int limit) {
    Query q = new Query(Criteria.where("user_id").in(userId, userId.toString()))
        .with(Sort.by(desc("created_at"), desc("_id")))
        .limit(limit + 1);

    List<UserInterestSubscriptionDoc> rows =
        mongo.find(q, UserInterestSubscriptionDoc.class, "user_interest_subscriptions");

    boolean more = rows.size() > limit;
    if (more) rows = new ArrayList<>(rows.subList(0, limit));

    CursorKey lastKey = rows.isEmpty()
        ? null
        : new CursorKey(pickInstant(rows.get(rows.size() - 1).getCreatedAt()), rows.get(rows.size() - 1).getId());

    return new RepoSlice<>(rows, more, lastKey);
  }

  // ===== Mongo: 댓글 =====
  @Override
  public RepoSlice<ActivityCommentDoc> findComments(UUID userId, int limit, CursorKey cursor) {
    Query q = buildCursorQuery(userId, limit, cursor, true, "created_at");
    List<ActivityCommentDoc> rows =
        mongo.find(q, ActivityCommentDoc.class, "activity_comments");

    boolean more = rows.size() > limit;
    if (more) rows = new ArrayList<>(rows.subList(0, limit));

    CursorKey lastKey = rows.isEmpty()
        ? null
        : new CursorKey(
            pickInstant(rows.get(rows.size() - 1).getCreatedAt()),
            rows.get(rows.size() - 1).getId()
        );

    return new RepoSlice<>(rows, more, lastKey);
  }

  // ===== Mongo: 댓글 좋아요 =====
  @Override
  public RepoSlice<ActivityCommentLikeDoc> findCommentLikes(UUID userId, int limit, CursorKey cursor) {
    Query q = buildCursorQuery(userId, limit, cursor, false, "created_at");
    List<ActivityCommentLikeDoc> rows =
        mongo.find(q, ActivityCommentLikeDoc.class, "activity_comment_likes");

    boolean more = rows.size() > limit;
    if (more) rows = new ArrayList<>(rows.subList(0, limit));

    CursorKey lastKey = rows.isEmpty()
        ? null
        : new CursorKey(
            pickInstant(rows.get(rows.size() - 1).getCreatedAt()),
            rows.get(rows.size() - 1).getId()
        );

    return new RepoSlice<>(rows, more, lastKey);
  }

  // ===== Mongo: 기사 열람 =====
  @Override
  public RepoSlice<ActivityArticleViewDoc> findArticleViews(UUID userId, int limit, CursorKey cursor) {
    // 인덱스와 정렬을 last_viewed_at 기준으로 통일
    Query q = buildCursorQuery(userId, limit, cursor, false, "last_viewed_at");
    List<ActivityArticleViewDoc> rows =
        mongo.find(q, ActivityArticleViewDoc.class, "activity_article_views");

    boolean more = rows.size() > limit;
    if (more) rows = new ArrayList<>(rows.subList(0, limit));

    CursorKey lastKey = rows.isEmpty()
        ? null
        : new CursorKey(
            pickInstant(rows.get(rows.size() - 1).getLastViewedAt()),
            rows.get(rows.size() - 1).getId()
        );

    return new RepoSlice<>(rows, more, lastKey);
  }

  // ===== 공통 유틸 =====
  private static Instant toInstant(Object v) {
    if (v instanceof Instant i) return i;
    if (v instanceof Timestamp ts) return ts.toInstant();
    if (v instanceof java.time.OffsetDateTime odt) return odt.toInstant();
    if (v instanceof java.time.LocalDateTime ldt) {
      return ldt.atOffset(java.time.ZoneOffset.UTC).toInstant();
    }
    throw new IllegalStateException("Unexpected created_at type: " + v);
  }

  private static Instant pickInstant(Instant i) { return i; }

  private Query buildCursorQuery(UUID userId, int limit, CursorKey cursor, boolean filterDeleted, String timeField) {
    // 혼재 대응: user_id 가 UUID/문자열 양쪽 있을 수 있음
    Criteria base = Criteria.where("user_id").in(userId, userId.toString());
    if (filterDeleted) base = base.and("is_deleted").ne(true);

    Criteria crit = base;
    if (cursor != null) {
      // created_at/last_viewed_at 등 선택된 시간 필드 기준 커서
      Criteria older = new Criteria().orOperator(
          Criteria.where(timeField).lt(cursor.createdAt()),
          new Criteria().andOperator(
              Criteria.where(timeField).is(cursor.createdAt()),
              Criteria.where("_id").lt(cursor.id())
          )
      );
      crit = new Criteria().andOperator(base, older);
    }

    return new Query(crit)
        .with(Sort.by(desc(timeField), desc("_id")))
        .limit(limit + 1);
  }
}