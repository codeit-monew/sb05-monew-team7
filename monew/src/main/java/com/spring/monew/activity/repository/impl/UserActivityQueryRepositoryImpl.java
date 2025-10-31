package com.spring.monew.activity.repository.impl;

import static org.springframework.data.domain.Sort.Order.desc;
import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import com.spring.monew.activity.repository.UserActivityQueryRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

  @Override
  public Optional<UserSummary> findUserSummary(UUID userId) {
    try {
      return Optional.ofNullable(
          jdbc.queryForObject(
              "SELECT id, email, nickname, created_at FROM users WHERE id = ?",
              (rs, rowNum) -> new UserSummary(
                  uuid(rs, "id"), rs.getString("email"), rs.getString("nickname"),
                  rs.getTimestamp("created_at") != null
                      ? rs.getTimestamp("created_at").toInstant()
                      : Instant.now()
              ),
              userId
          )
      );
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  @Override
  public List<UserActivityDto.Subscription> findTopSubscriptions(UUID userId, int topN) {
    Query q = new Query();
    q.addCriteria(Criteria.where("userId").is(userId));
    q.with(Sort.by(desc("createdAt"), desc("_id")));
    q.limit(topN);
    List<UserInterestSubscriptionDoc> docs = mongo.find(q, UserInterestSubscriptionDoc.class);
    List<UserActivityDto.Subscription> out = new ArrayList<UserActivityDto.Subscription>(docs.size());
    for (UserInterestSubscriptionDoc d : docs) {
      out.add(new UserActivityDto.Subscription(
          UUID.fromString(d.getId()), d.getInterestId(), d.getInterestName(),
          d.getInterestKeywords(), d.getInterestSubscriberCount(), d.getCreatedAt()
      ));
    }
    return out;
  }

  @Override
  public List<UserActivityDto.Comment> findTopComments(UUID userId, int topN) {
    Query q = new Query();
    q.addCriteria(Criteria.where("userId").is(userId));
    q.with(Sort.by(desc("createdAt"), desc("_id")));
    q.limit(topN);
    List<ActivityCommentDoc> docs = mongo.find(q, ActivityCommentDoc.class);
    List<UserActivityDto.Comment> out = new ArrayList<UserActivityDto.Comment>(docs.size());
    for (ActivityCommentDoc d : docs) {
      out.add(new UserActivityDto.Comment(
          UUID.fromString(d.getId()), d.getArticleId(), d.getArticleTitle(),
          d.getUserId(), null, d.getContent(), d.getLikeCount(), d.getCreatedAt()
      ));
    }
    return out;
  }

  @Override
  public List<UserActivityDto.CommentLike> findTopCommentLikes(UUID userId, int topN) {
    Query q = new Query();
    q.addCriteria(Criteria.where("userId").is(userId));
    q.with(Sort.by(desc("createdAt"), desc("_id")));
    q.limit(topN);
    List<ActivityCommentLikeDoc> docs = mongo.find(q, ActivityCommentLikeDoc.class);
    List<UserActivityDto.CommentLike> out = new ArrayList<UserActivityDto.CommentLike>(docs.size());
    for (ActivityCommentLikeDoc d : docs) {
      out.add(new UserActivityDto.CommentLike(
          UUID.fromString(d.getId()), d.getCreatedAt(), d.getCommentId(), d.getArticleId(), d.getArticleTitle(),
          d.getCommentUserId(), d.getCommentUserNickname(), d.getCommentContent(), d.getCommentLikeCount(), d.getCommentCreatedAt()
      ));
    }
    return out;
  }

  // article views
  @Override
  public List<UserActivityDto.ArticleView> findTopArticleViews(UUID userId, int topN) {
    Query q = new Query();
    q.addCriteria(Criteria.where("userId").is(userId));
    q.with(Sort.by(desc("createdAt"), desc("_id")));
    q.limit(topN);
    List<ActivityArticleViewDoc> docs = mongo.find(q, ActivityArticleViewDoc.class);
    List<UserActivityDto.ArticleView> out = new ArrayList<UserActivityDto.ArticleView>(docs.size());
    for (ActivityArticleViewDoc d : docs) {
      out.add(new UserActivityDto.ArticleView(
          UUID.fromString(d.getId()), d.getUserId(), d.getCreatedAt(), d.getArticleId(), d.getSource(), d.getSourceUrl(),
          d.getTitle(), d.getPublishDate(), d.getSummary(), d.getCommentCount(), d.getViewCount()
      ));
    }
    return out;
  }

  private static UUID uuid(ResultSet rs, String col) throws SQLException {
    Object obj = rs.getObject(col);
    if (obj instanceof UUID) {
      return (UUID) obj;
    }
    return UUID.fromString(Objects.toString(obj));
  }
}