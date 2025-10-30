package com.spring.monew.activity.repository.impl;

import com.spring.monew.activity.repository.ActivitySyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivitySyncServiceImpl implements ActivitySyncService {

  private final MongoTemplate mongo;

  // Mongo collections
  private static final String COL_SUBSCRIPTIONS     = "user_interest_subscriptions";
  private static final String COL_COMMENTS          = "activity_comments";
  private static final String COL_COMMENT_LIKES     = "activity_comment_likes";
  private static final String COL_ARTICLE_VIEWS     = "activity_article_views";

  // 구독 (키: user_id + interest_id)
  @Override
  public void onSubscribed(
      UUID subscriptionId,
      UUID userId,
      UUID interestId,
      String interestName,
      List<String> interestKeywords,
      long interestSubscriberCount,
      Instant createdAt
  ) {
    Query q = Query.query(Criteria.where("user_id").is(userId).and("interest_id").is(interestId));
    Update u = new Update()
        .setOnInsert("_id", subscriptionId) // 문서 식별자 저장(고유성은 복합키로 보장)
        .setOnInsert("user_id", userId)
        .setOnInsert("interest_id", interestId)
        .setOnInsert("created_at", nonNull(createdAt))
        .set("interest_name", interestName)
        .set("interest_keywords", interestKeywords)
        .set("interest_subscriber_count", interestSubscriberCount);

    mongo.findAndModify(q, u,
        FindAndModifyOptions.options().upsert(true).returnNew(true),
        Object.class, COL_SUBSCRIPTIONS);
  }

  @Override
  public void onUnsubscribed(UUID userId, UUID interestId) {
    Query q = Query.query(Criteria.where("user_id").is(userId).and("interest_id").is(interestId));
    mongo.remove(q, COL_SUBSCRIPTIONS);
  }

  // 댓글 (키: _id = commentId)
  @Override
  public void onCommentCreated(
      UUID commentId,
      UUID userId,
      UUID articleId,
      String articleTitleSnapshot,
      String userNicknameSnapshot,
      String content,
      long likeCount,
      Instant createdAt
  ) {
    Query q = Query.query(Criteria.where("_id").is(commentId));
    Update u = new Update()
        .setOnInsert("_id", commentId)
        .setOnInsert("user_id", userId)
        .setOnInsert("article_id", articleId)
        .set("article_title", articleTitleSnapshot)
        .set("user_nickname", userNicknameSnapshot)
        .set("content", content)
        .set("like_count", likeCount)
        .setOnInsert("created_at", nonNull(createdAt))
        .setOnInsert("is_deleted", false)
        .unset("deleted_at");

    mongo.findAndModify(q, u,
        FindAndModifyOptions.options().upsert(true).returnNew(true),
        Object.class, COL_COMMENTS);
  }

  @Override
  public void onCommentDeleted(UUID commentId, Instant deletedAt) {
    Query q = Query.query(Criteria.where("_id").is(commentId));
    Update u = new Update()
        .set("is_deleted", true)
        .set("deleted_at", nonNull(deletedAt));
    mongo.updateFirst(q, u, COL_COMMENTS);
  }

  @Override
  public void onCommentLiked(
      UUID likeEventId,
      UUID likedByUserId,
      UUID commentId,
      UUID articleId,
      String articleTitleSnapshot,
      UUID commentUserId,
      String commentUserNicknameSnapshot,
      String commentContentSnapshot,
      long commentLikeCountSnapshot,
      Instant commentCreatedAtSnapshot,// ← 추가
      Instant likedAt
  ) {
    // 좋아요 이벤트 스냅샷 upsert
    Query qLike = Query.query(Criteria.where("_id").is(likeEventId));
    Update uLike = new Update()
        .setOnInsert("_id", likeEventId)
        .setOnInsert("created_at", nonNull(likedAt))
        .set("user_id", likedByUserId)
        .set("comment_id", commentId)
        .set("article_id", articleId)
        .set("article_title", articleTitleSnapshot)
        .set("comment_user_id", commentUserId)
        .set("comment_user_nickname", commentUserNicknameSnapshot)
        .set("comment_content", commentContentSnapshot)
        .set("comment_like_count", commentLikeCountSnapshot);

    mongo.findAndModify(
        qLike, uLike,
        FindAndModifyOptions.options().upsert(true).returnNew(true),
        Object.class, COL_COMMENT_LIKES
    );

    // 댓글 본문 문서의 like_count 스냅샷도 갱신 (없으면 생성)
    Query qC = Query.query(Criteria.where("_id").is(commentId));
    Update uC = new Update()
        .setOnInsert("created_at", nonNull(commentCreatedAtSnapshot))
        .set("like_count", commentLikeCountSnapshot);
    mongo.findAndModify(
        qC, uC,
        FindAndModifyOptions.options().upsert(true).returnNew(true),
        Object.class, COL_COMMENTS
    );
  }


  @Override
  public void onCommentLikeCanceled(UUID likeEventId) {
    Query q = Query.query(Criteria.where("_id").is(likeEventId));
    mongo.remove(q, COL_COMMENT_LIKES);
  }

  // 기사 조회 (유저×기사 단건 문서 유지, 카운트/최종시각만 증가)
  @Override
  public void onArticleViewed(
      UUID viewEventId,
      UUID userId,
      UUID articleId,
      String source,
      String sourceUrl,
      String articleTitleSnapshot,
      Instant articlePublishDateSnapshot,
      String articleSummarySnapshot,
      long articleCommentCountSnapshot,
      long articleViewCountSnapshot,
      Instant viewedAt
  ) {
    // 동일 유저가 같은 기사를 보는 단일 레코드: (user_id, article_id)
    Query q = Query.query(Criteria.where("user_id").is(userId).and("article_id").is(articleId));

    // 최초 생성 시 스냅샷 채우고 view_count_by_user=1
    var existing = mongo.findOne(q, org.bson.Document.class, COL_ARTICLE_VIEWS);
    if (existing == null) {
      Update u = new Update()
          .setOnInsert("_id", viewEventId)
          .setOnInsert("user_id", userId)
          .setOnInsert("article_id", articleId)
          .setOnInsert("source", source)
          .setOnInsert("source_url", sourceUrl)
          .setOnInsert("article_title", articleTitleSnapshot)
          .setOnInsert("article_published_date", articlePublishDateSnapshot)
          .setOnInsert("article_summary", articleSummarySnapshot)
          .setOnInsert("article_comment_count", articleCommentCountSnapshot)
          .setOnInsert("article_view_count", articleViewCountSnapshot)
          .setOnInsert("created_at", nonNull(viewedAt))
          .set("last_viewed_at", nonNull(viewedAt))
          .setOnInsert("view_count_by_user", 1L);

      mongo.findAndModify(q, u,
          FindAndModifyOptions.options().upsert(true).returnNew(true),
          Object.class, COL_ARTICLE_VIEWS);
      return;
    }

    // 존재하면 마지막 본 시각을 갱신하고 카운트 +1
    Update u = new Update()
        .set("last_viewed_at", nonNull(viewedAt))
        .inc("view_count_by_user", 1L);
    mongo.updateFirst(q, u, COL_ARTICLE_VIEWS);
  }
  // util
  private static Instant nonNull(Instant t) {
    return (t != null) ? t : Instant.now();
  }
}
