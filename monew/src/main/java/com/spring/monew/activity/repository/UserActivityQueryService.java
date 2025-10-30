package com.spring.monew.activity.repository;

import com.spring.monew.activity.controller.dto.response.UserActivityResponse;
import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Order.desc;

@Service
@RequiredArgsConstructor
public class UserActivityQueryService {

  private final MongoTemplate mongo;
  private final JdbcTemplate jdbc;

  @Transactional(readOnly = true)
  public UserActivityResponse getUserActivity(UUID userId, int limit, String cursorStr) {
    // 1) 사용자 요약 (RDB)
    UserSummary user = fetchUserSummary(userId);

    // 2) 커서
    Cursor cursor = decodeCursor(cursorStr);

    // 3) 각 섹션 조회 (limit는 클램프)
    int pageSize = clamp(limit);
    List<UserActivityResponse.Subscription> subs =
        findSubs(userId, pageSize).items();
    List<UserActivityResponse.Comment> comments =
        findComments(userId, pageSize, cursor).items();
    List<UserActivityResponse.CommentLike> likes =
        findLikes(userId, pageSize, cursor).items();
    List<UserActivityResponse.ArticleView> views =
        findViews(userId, pageSize, cursor).items();

    // 4) 응답 조립
    return new UserActivityResponse(
        user.id(), user.email(), user.nickname(), user.createdAt(),
        subs, comments, likes, views
    );
  }

  // ===== 사용자 요약 (RDB) =====
  private UserSummary fetchUserSummary(UUID userId) {
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

  private static Instant toInstant(Object v) {
    if (v instanceof Instant i) return i;
    if (v instanceof Timestamp ts) return ts.toInstant();
    if (v instanceof java.time.OffsetDateTime odt) return odt.toInstant();
    if (v instanceof java.time.LocalDateTime ldt) {
      return ldt.atOffset(java.time.ZoneOffset.UTC).toInstant();
    }
    throw new IllegalStateException("Unexpected created_at type: " + v);
  }

  private record UserSummary(UUID id, String email, String nickname, Instant createdAt) {}

  // ===== 구독 (최신 N건) =====
  private Slice<UserActivityResponse.Subscription> findSubs(UUID userId, int limit) {
    // user_id 타입 미스매치 방지: UUID/문자열 모두 매칭
    Criteria crit = Criteria.where("user_id").in(userId, userId.toString());
    Query q = new Query(crit)
        .with(Sort.by(desc("created_at"), desc("_id")))
        .limit(limit + 1);

    List<UserInterestSubscriptionDoc> rows =
        mongo.find(q, UserInterestSubscriptionDoc.class, "user_interest_subscriptions");

    boolean more = rows.size() > limit;
    if (more) rows.remove(rows.size() - 1);

    List<UserActivityResponse.Subscription> items = rows.stream()
        .map(d -> new UserActivityResponse.Subscription(
            d.getId(),
            d.getInterestId(),
            d.getInterestName(),
            d.getInterestKeywords(),
            d.getInterestSubscriberCount(),
            pickInstant(d.getCreatedAt())
        ))
        .toList();

    String next = (!items.isEmpty() && more)
        ? encodeCursor(items.get(items.size() - 1).createdAt(), items.get(items.size() - 1).id())
        : null;

    return new Slice<>(items, more, next);
  }

  // ===== 댓글(커서) =====
  private Slice<UserActivityResponse.Comment> findComments(UUID userId, int limit, Cursor cursor) {
    Query q = buildCursorQuery(userId, limit, cursor, true);
    List<ActivityCommentDoc> rows =
        mongo.find(q, ActivityCommentDoc.class, "activity_comments");

    boolean more = rows.size() > limit;
    if (more) rows.remove(rows.size() - 1);

    List<UserActivityResponse.Comment> items = rows.stream()
        .map(d -> new UserActivityResponse.Comment(
            d.getId(),
            d.getArticleId(),
            nz(d.getArticleTitle()),
            d.getUserId(),
            nz(d.getUserNickname()),
            nz(d.getContent()),
            d.getLikeCount(),
            pickInstant(d.getCreatedAt())
        ))
        .toList();

    String next = (!items.isEmpty() && more)
        ? encodeCursor(items.get(items.size() - 1).createdAt(), items.get(items.size() - 1).id())
        : null;

    return new Slice<>(items, more, next);
  }

  // ===== 좋아요(커서) =====
  private Slice<UserActivityResponse.CommentLike> findLikes(UUID userId, int limit, Cursor cursor) {
    // created_at DESC, _id DESC 정렬(인덱스와 일치)
    Query q = buildCursorQuery(userId, limit, cursor, false);

    List<ActivityCommentLikeDoc> rows =
        mongo.find(q, ActivityCommentLikeDoc.class, "activity_comment_likes");

    boolean more = rows.size() > limit;
    if (more) rows.remove(rows.size() - 1);
    // 화면 표시는 created_at 우선
    List<UserActivityResponse.CommentLike> items = rows.stream()
        .map(d -> new UserActivityResponse.CommentLike(
            d.getId(),
            pickInstant(d.getCreatedAt(), d.getCommentCreatedAt()),
            d.getCommentId(),
            d.getArticleId(),
            nz(d.getArticleTitle()),
            d.getCommentUserId(),
            nz(d.getCommentUserNickname()),
            nz(d.getCommentContent()),
            d.getCommentLikeCount(),
            /* commentCreatedAt: 보조로 전달 */
            pickInstant(d.getCommentCreatedAt())
        ))
        .sorted(
            java.util.Comparator
                .comparing(UserActivityResponse.CommentLike::createdAt,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())) // ASC
                .thenComparing(UserActivityResponse.CommentLike::id) // 동률일 때 안정적 정렬
        )
        .toList();

    // 커서는 정렬 키(created_at) 기준으로 생성
    String next = null;
    if (!rows.isEmpty() && more) {
      ActivityCommentLikeDoc lastDoc = rows.get(rows.size() - 1);
      Instant cursorCreated = (lastDoc.getCreatedAt() != null)
          ? lastDoc.getCreatedAt()
          : Instant.EPOCH; // created_at이 null
      next = encodeCursor(cursorCreated, lastDoc.getId());
    }

    return new Slice<>(items, more, next);
  }

  // ===== 기사뷰(커서) =====
  private Slice<UserActivityResponse.ArticleView> findViews(UUID userId, int limit, Cursor cursor) {
    Query q = buildCursorQuery(userId, limit, cursor, false);
    List<ActivityArticleViewDoc> rows =
        mongo.find(q, ActivityArticleViewDoc.class, "activity_article_views");

    boolean more = rows.size() > limit;
    if (more) rows.remove(rows.size() - 1);

    List<UserActivityResponse.ArticleView> items = rows.stream()
        .map(d -> new UserActivityResponse.ArticleView(
            d.getId(),
            userId,
            // createdAt이 없으면 기사 발행일을 후보로 사용
            pickInstant(d.getCreatedAt(), d.getArticlePublishedDate()),
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

    String next = (!items.isEmpty() && more)
        ? encodeCursor(items.get(items.size() - 1).createdAt(), items.get(items.size() - 1).id())
        : null;

    return new Slice<>(items, more, next);
  }

  // ===== 공통: 커서/쿼리 빌더 =====
  private Query buildCursorQuery(UUID userId, int limit, Cursor cursor, boolean filterDeleted) {
    // user_id가 UUID 또는 문자열로 저장됐을 수 있음
    Criteria base = Criteria.where("user_id").in(userId, userId.toString());
    if (filterDeleted) base = base.and("is_deleted").ne(true);

    Criteria crit = base;
    if (cursor != null) {
      Criteria older = new Criteria().orOperator(
          Criteria.where("created_at").lt(cursor.createdAt()),
          new Criteria().andOperator(
              Criteria.where("created_at").is(cursor.createdAt()),
              // _id가 문자열 UUID일 가능성 대비
              Criteria.where("_id").lt(cursor.id().toString())
          )
      );
      crit = new Criteria().andOperator(base, older);
    }

    return new Query(crit)
        .with(Sort.by(desc("created_at"), desc("_id")))
        .limit(limit + 1);
  }

  // ===== 유틸 =====
  private static int clamp(int limit) {
    return Math.max(1, Math.min(limit, 100));
  }

  private static String nz(String v) { return v == null ? "" : v; }
  // 가장 먼저 존재하는 Instant를 선택, 전부 null이면 현재시각 사용
  private static Instant pickInstant(Instant... cands) {
    for (Instant i : cands) if (i != null) return i;
    return null; // now() 금지
  }
  private record Cursor(Instant createdAt, UUID id) {}

  private String encodeCursor(Instant createdAt, UUID id) {
    String raw = createdAt.toEpochMilli() + "|" + id;
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
  }

  private Cursor decodeCursor(String cursor) {
    if (cursor == null || cursor.isBlank()) return null;
    try {
      String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
      String[] parts = raw.split("\\|");
      return new Cursor(
          Instant.ofEpochMilli(Long.parseLong(parts[0])),
          UUID.fromString(parts[1])
      );
    } catch (Exception e) {
      // 손상된 커서는 무시하고 첫 페이지로 간주
      return null;
    }
  }

  private record Slice<T>(List<T> items, boolean hasMore, String nextCursor) {}
}
