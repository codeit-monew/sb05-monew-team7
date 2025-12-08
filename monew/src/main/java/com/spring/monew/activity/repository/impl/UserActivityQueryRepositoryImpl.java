package com.spring.monew.activity.repository.impl;

import static org.springframework.data.domain.Sort.Order.desc;

import com.spring.monew.activity.controller.dto.response.CommentActivityDto;
import com.spring.monew.activity.controller.dto.response.CommentLikeActivityDto;
import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import com.spring.monew.activity.repository.UserActivityQueryRepository;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
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
  private final ArticleRepository articleRepository;

  @Override
  public Optional<UserSummary> findUserSummary(UUID userId) {
    try {
      return Optional.ofNullable(
          jdbc.queryForObject(
              "SELECT id, email, nickname, created_at FROM users WHERE id = ?",
              (rs, rowNum) -> new UserSummary(
                  uuid(rs, "id"), rs.getString("email"), rs.getString("nickname"),
                  rs.getTimestamp("created_at").toInstant()  // NPE 발생 시 데이터 문제로 간주
              ),
              userId
          )
      );
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<SubscriptionDto> findTopSubscriptions(UUID userId, int topN) {
    Query q = new Query();
    q.addCriteria(Criteria.where("userId").is(userId));
    q.with(Sort.by(desc("createdAt"), desc("_id")));
    q.limit(topN);
    List<UserInterestSubscriptionDoc> docs = mongo.find(q, UserInterestSubscriptionDoc.class);
    List<SubscriptionDto> out = new ArrayList<SubscriptionDto>(
        docs.size());
    for (UserInterestSubscriptionDoc d : docs) {
      out.add(new SubscriptionDto(
          UUID.fromString(d.getId()), d.getInterestId(), d.getInterestName(),
          d.getInterestKeywords(), d.getInterestSubscriberCount(), d.getCreatedAt()
      ));
    }
    return out;
  }

  @Override
  public List<CommentActivityDto> findTopComments(UUID userId, int topN) {
    Query q = new Query();
    q.addCriteria(Criteria.where("userId").is(userId));
    q.with(Sort.by(desc("createdAt"), desc("_id")));
    q.limit(topN);

    List<ActivityCommentDoc> docs = mongo.find(q, ActivityCommentDoc.class);
    List<CommentActivityDto> out = new ArrayList<>(docs.size());

    for (ActivityCommentDoc d : docs) {
      // ① 스냅샷에서 우선 가져온다
      String nickname = d.getUserNickname();

      // ② 과거 스냅샷(닉네임 비어 있음) 폴백: RDB에서 한 번 더 조회
      if (nickname == null || nickname.isBlank()) {
        nickname = findNicknameFallback(d.getUserId());
        if (nickname == null || nickname.isBlank()) {
          nickname = "(알 수 없음)";
        }
      }
      out.add(new CommentActivityDto(
          UUID.fromString(d.getId()),
          d.getArticleId(),
          d.getArticleTitle(),
          d.getUserId(),
          nickname,
          d.getContent(),
          d.getLikeCount(),
          d.getCreatedAt()
      ));
    }
    return out;
  }

  private String findNicknameFallback(UUID userId) {
    try {
      // users 테이블에 nickname 컬럼 있는 전제
      return jdbc.queryForObject(
          "SELECT nickname FROM users WHERE id = ?",
          String.class,
          userId
      );
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  @Override
  public List<CommentLikeActivityDto> findTopCommentLikes(UUID userId, int topN) {
    Query q = new Query();
    q.addCriteria(Criteria.where("userId").is(userId));
    q.with(Sort.by(desc("createdAt"), desc("_id")));
    q.limit(topN);
    List<ActivityCommentLikeDoc> docs = mongo.find(q, ActivityCommentLikeDoc.class);
    List<CommentLikeActivityDto> out = new ArrayList<CommentLikeActivityDto>(
        docs.size());
    for (ActivityCommentLikeDoc d : docs) {
      out.add(new CommentLikeActivityDto(
          UUID.fromString(d.getId()), d.getCreatedAt(), d.getCommentId(), d.getArticleId(),
          d.getArticleTitle(),
          d.getCommentUserId(), d.getCommentUserNickname(), d.getCommentContent(),
          d.getCommentLikeCount(), d.getCommentCreatedAt()
      ));
    }
    return out;
  }

  // article views
  @Override
  public List<ArticleViewDto> findTopArticleViews(UUID userId, int topN) {
    Query q = new Query();
    q.addCriteria(Criteria.where("userId").is(userId));
    q.with(Sort.by(desc("lastViewedAt"), desc("_id")));
    q.limit(topN);

    List<ActivityArticleViewDoc> docs = mongo.find(q, ActivityArticleViewDoc.class);
    if (docs.isEmpty()) return List.of();

    List<UUID> articleIds = docs.stream()
        .map(ActivityArticleViewDoc::getArticleId)
        .distinct()
        .toList();

    Map<UUID, Long> latestCc = articleRepository.findAllById(articleIds).stream()
        .collect(Collectors.toMap(
            Article::getId,
            a -> a.getCommentCount() == 0 ? 0L : a.getCommentCount()
        ));

    List<ArticleViewDto> out = new ArrayList<>(docs.size());
    for (ActivityArticleViewDoc d : docs) {
      // ★ 변경: 댓글수는 RDB 최신값 우선, 없으면 Mongo 값 사용
      long cc = latestCc.getOrDefault(
          d.getArticleId(),
          d.getCommentCount() == null ? 0L : d.getCommentCount()
      );
      long vc = d.getViewCount() == null ? 0L : d.getViewCount();

      out.add(new ArticleViewDto(
          UUID.fromString(d.getId()),
          d.getUserId(),
          d.getLastViewedAt(),
          d.getArticleId(),
          d.getSource(),
          d.getSourceUrl(),
          d.getTitle(),
          d.getPublishDate(),
          d.getSummary(),
          cc,
          vc
      ));
    }
    return out;
  }

  // === 클래스 레벨(아래처럼 메서드 밖) ===
  private static UUID uuid(ResultSet rs, String col) throws SQLException {
    Object obj = rs.getObject(col);
      if (obj instanceof UUID u) {
          return u;      // 자바 17 패턴 매칭
      }
    return UUID.fromString(Objects.toString(obj));
  }
}