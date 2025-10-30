// monew/src/main/java/com/spring/monew/activity/repository/impl/ActivitySyncRepositoryImpl.java
package com.spring.monew.activity.repository.impl;

import com.spring.monew.activity.repository.ActivitySyncRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ActivitySyncRepositoryImpl implements ActivitySyncRepository {

  private final MongoTemplate mongo;

  // ===== 컬렉션 이름(조회/리드 모델) =====
  private static final String COL_SUBS     = "user_interest_subscriptions";
  private static final String COL_COMMENTS = "activity_comments";
  private static final String COL_LIKES    = "activity_comment_likes";
  private static final String COL_VIEWS    = "activity_article_views";

  // ===== 구독 =====
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
    Objects.requireNonNull(subscriptionId, "subscriptionId");
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(interestId, "interestId");

    // (user_id, interest_id) 복합키 기준 upsert — UUID/문자열 혼재 대응
    Query q = new Query(new Criteria().andOperator(
        Criteria.where("user_id").in(userId, userId.toString()),
        Criteria.where("interest_id").in(interestId, interestId.toString())
    ));

    Update u = new Update()
        .setOnInsert("_id", subscriptionId)
        .setOnInsert("user_id", userId.toString())
        .setOnInsert("interest_id", interestId.toString())
        .setOnInsert("created_at", createdAt != null ? createdAt : Instant.now())
        .set("interest_name", nz(interestName))
        .set("interest_keywords", interestKeywords)
        .set("interest_subscriber_count", interestSubscriberCount)
        .set("is_deleted", false)
        .unset("deleted_at");

    mongo.findAndModify(q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), Object.class, COL_SUBS);
    log.debug("[ActivitySync] subscribed upsert: subId={}, user={}, interest={}", subscriptionId, userId, interestId);
  }

  @Override
  public void onUnsubscribed(UUID userId, UUID interestId) {
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(interestId, "interestId");

    Query q = new Query(new Criteria().andOperator(
        Criteria.where("user_id").in(userId, userId.toString()),
        Criteria.where("interest_id").in(interestId, interestId.toString())
    ));
    mongo.remove(q, COL_SUBS);
    log.debug("[ActivitySync] unsubscribed remove: user={}, interest={}", userId, interestId);
  }

  // ===== 댓글 =====
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
    Objects.requireNonNull(commentId, "commentId");

    Query q = byEventId(commentId);
    Update u = new Update()
        .setOnInsert("_id", commentId)
        .setOnInsert("created_at", createdAt)
        .set("user_id", userId != null ? userId.toString() : null)
        .set("article_id", articleId != null ? articleId.toString() : null)
        .set("article_title", nz(articleTitleSnapshot))
        .set("user_nickname", nz(userNicknameSnapshot))
        .set("content", nz(content))
        .set("like_count", likeCount)
        .set("is_deleted", false)
        .unset("deleted_at");

    mongo.findAndModify(q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), Object.class, COL_COMMENTS);
    log.debug("[ActivitySync] comment created upsert: commentId={}, article={}", commentId, articleId);
  }

  @Override
  public void onCommentDeleted(UUID commentId, Instant deletedAt) {
    Objects.requireNonNull(commentId, "commentId");
    Query q = byEventId(commentId);
    Update u = new Update()
        .set("is_deleted", true)
        .set("deleted_at", deletedAt);
    mongo.updateFirst(q, u, COL_COMMENTS);
    log.debug("[ActivitySync] comment deleted: commentId={}", commentId);
  }

  // ===== 댓글 좋아요 =====
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
      Instant commentCreatedAtSnapshot,
      Instant likedAt
  ) {
    Objects.requireNonNull(likeEventId, "likeEventId");

    Query q = byEventId(likeEventId);
    Update u = new Update()
        .setOnInsert("_id", likeEventId)
        .set("liked_by_user_id", likedByUserId != null ? likedByUserId.toString() : null)
        .set("comment_id", commentId != null ? commentId.toString() : null)
        .set("article_id", articleId != null ? articleId.toString() : null)
        .set("article_title", nz(articleTitleSnapshot))
        .set("comment_user_id", commentUserId != null ? commentUserId.toString() : null)
        .set("comment_user_nickname", nz(commentUserNicknameSnapshot))
        .set("comment_content", nz(commentContentSnapshot))
        .set("comment_like_count", commentLikeCountSnapshot)
        .set("comment_created_at", commentCreatedAtSnapshot)
        .set("created_at", likedAt);

    mongo.findAndModify(q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), Object.class, COL_LIKES);
    log.debug("[ActivitySync] comment liked upsert: likeId={}, comment={}", likeEventId, commentId);
  }

  @Override
  public void onCommentLikeCanceled(UUID likeEventId) {
    Objects.requireNonNull(likeEventId, "likeEventId");
    mongo.remove(byEventId(likeEventId), COL_LIKES);
    log.debug("[ActivitySync] comment like canceled: likeId={}", likeEventId);
  }

  // ===== 기사 조회 =====
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
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(articleId, "articleId");

    Instant when = viewedAt != null ? viewedAt : Instant.now();

    // (user_id, article_id) 복합키 기준 upsert — UUID/문자열 혼재 대응
    Query q = new Query(new Criteria().andOperator(
        Criteria.where("user_id").in(userId, userId.toString()),
        Criteria.where("article_id").in(articleId, articleId.toString())
    ));

    Update u = new Update()
        // 최초 생성 시 스냅샷
        .setOnInsert("_id", viewEventId)
        .setOnInsert("user_id", userId.toString())
        .setOnInsert("article_id", articleId.toString())
        .setOnInsert("source", nz(source))
        .setOnInsert("source_url", nz(sourceUrl))
        .setOnInsert("article_title", nz(articleTitleSnapshot))
        .setOnInsert("article_published_date", articlePublishDateSnapshot)
        .setOnInsert("article_summary", nz(articleSummarySnapshot))
        .setOnInsert("article_comment_count", articleCommentCountSnapshot)
        .setOnInsert("article_view_count", articleViewCountSnapshot)
        .setOnInsert("created_at", when)
        .setOnInsert("view_count_by_user", 0L)     // 아래 inc 로 1이 됨
        // 항상 갱신되는 필드
        .set("last_viewed_at", when)
        .inc("view_count_by_user", 1L);

    mongo.findAndModify(q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), Object.class, COL_VIEWS);
    log.debug("[ActivitySync] article viewed upsert: viewId={}, user={}, article={}", viewEventId, userId, articleId);
  }

  // ===== 유틸 =====
  private static Query byEventId(UUID id) {
    // _id 가 UUID/문자열로 섞여 저장된 과거 데이터 대비
    return new Query(new Criteria().orOperator(
        Criteria.where("_id").is(id),
        Criteria.where("_id").is(id != null ? id.toString() : null)
    ));
  }

  private static String nz(String v) {
    return v == null ? "" : v;
  }
}
