package com.spring.monew.activity.repository.impl;

import com.mongodb.client.result.UpdateResult;
import com.spring.monew.activity.domain.ActivityCommentDoc;
import com.spring.monew.activity.domain.ActivityCommentLikeDoc;
import com.spring.monew.activity.domain.UserInterestSubscriptionDoc;
import com.spring.monew.activity.repository.ActivitySyncRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ActivitySyncRepositoryImpl implements ActivitySyncRepository {

    private final MongoTemplate mongo;

    // ====== 구독 ======
    @Override
    public void onSubscribed(UUID subscriptionId, UUID userId, UUID interestId, String interestName,
        List<String> interestKeywords, long interestSubscriberCount, Instant createdAt) {
        if (subscriptionId == null || userId == null || interestId == null) {
            log.warn("onSubscribed skipped: subscriptionId={}, userId={}, interestId={}", subscriptionId, userId, interestId);
            return;
        }
        Update up = new Update()
            .set("user_id", userId)
            .set("interest_id", interestId)
            .set("interest_name", interestName)
            .set("interest_keywords", interestKeywords)
            .set("interest_subscriber_count", interestSubscriberCount)
            .set("created_at", createdAt);

        UpdateResult r = mongo.upsert(
            Query.query(Criteria.where("_id").is(subscriptionId.toString())),
            up, UserInterestSubscriptionDoc.class
        );
        log.info("onSubscribed -> matched={}, modified={}, upsertedId={}", r.getMatchedCount(), r.getModifiedCount(), r.getUpsertedId());

        if (r.getMatchedCount() == 0 && r.getUpsertedId() == null) {
            Query legacyKey = Query.query(
                Criteria.where("user_id").in(userId, userId.toString())
                    .and("interest_id").in(interestId, interestId.toString())
            );
            up.setOnInsert("_id", subscriptionId.toString());
            UpdateResult r2 = mongo.upsert(legacyKey, up, UserInterestSubscriptionDoc.class);
            log.info("onSubscribed[legacy-fallback] -> matched={}, modified={}, upsertedId={}",
                r2.getMatchedCount(), r2.getModifiedCount(), r2.getUpsertedId());
        }
    }

    @Override
    public void onUnsubscribed(UUID subscriptionId) {
        mongo.remove(Query.query(Criteria.where("_id").is(subscriptionId.toString())),
            UserInterestSubscriptionDoc.class);
    }

    @Override
    public void onUnsubscribed(UUID userId, UUID interestId) {
        Query q = Query.query(
            Criteria.where("user_id").in(userId, userId.toString())
                .and("interest_id").in(interestId, interestId.toString())
        );
        mongo.remove(q, UserInterestSubscriptionDoc.class);
    }

    // ====== 댓글 ======
    @Override
    public void onCommentCreated(UUID commentId, UUID userId, UUID articleId, String articleTitle,
        String commentUserNickname, String content, long likeCount, Instant createdAt) {
        Update up = new Update()
            .set("userId", userId)
            .set("articleId", articleId)
            .set("articleTitle", articleTitle)
            .set("commentUserNickname", commentUserNickname)
            .set("content", content)
            .set("likeCount", likeCount)
            .set("createdAt", createdAt);

        mongo.upsert(
            Query.query(Criteria.where("_id").is(commentId.toString())),
            up,
            ActivityCommentDoc.class
        );
    }

    @Override
    public void onCommentDeleted(UUID commentId) {
        mongo.remove(
            Query.query(Criteria.where("_id").is(commentId.toString())),
            ActivityCommentDoc.class
        );
    }

    @Override
    public void onCommentDeleted(UUID commentId, Instant deletedAt) {
        onCommentDeleted(commentId); // deletedAt은 현재 미사용
    }

    // ====== 댓글 좋아요 ======
    @Override
    public void onCommentLiked(UUID likeEventId, UUID userId, UUID commentId, UUID articleId, String articleTitle,
        UUID commentUserId, String commentUserNickname, String commentContent,
        long commentLikeCount, Instant commentCreatedAt, Instant likeCreatedAt) {

        // 1) 좋아요 이벤트 upsert
        Update up = new Update()
            .set("userId", userId) // likedBy
            .set("commentId", commentId)
            .set("articleId", articleId)
            .set("articleTitle", articleTitle)
            .set("commentUserId", commentUserId)
            .set("commentUserNickname", commentUserNickname)
            .set("commentContent", commentContent)
            .set("commentLikeCount", commentLikeCount)
            .set("commentCreatedAt", commentCreatedAt)
            .set("createdAt", likeCreatedAt);

        mongo.upsert(
            Query.query(Criteria.where("_id").is(likeEventId.toString())),
            up,
            ActivityCommentLikeDoc.class
        );

        // 2) 댓글 스냅샷의 likeCount 를 정답 스냅샷 값으로 동기화
        Query q = Query.query(Criteria.where("_id").is(commentId.toString()));
        Update sync = new Update()
            .set("likeCount", commentLikeCount)
            .set("lastLikedAt", likeCreatedAt);
        mongo.updateFirst(q, sync, ActivityCommentDoc.class);
    }

    @Override
    public void onCommentLikeCanceled(UUID likeId, UUID userId) {
        // 1) 어떤 댓글의 좋아요인지 먼저 조회
        ActivityCommentLikeDoc likeDoc =
            mongo.findById(likeId.toString(), ActivityCommentLikeDoc.class);

        // 2) like 문서 삭제
        mongo.remove(
            Query.query(Criteria.where("_id").is(likeId.toString())),
            ActivityCommentLikeDoc.class
        );

        // 3) 댓글 스냅샷의 likeCount 감소 (하한 0 보정)
        if (likeDoc != null && likeDoc.getCommentId() != null) {
            String commentKey = likeDoc.getCommentId().toString();

            // 3-1) 우선 1 감소
            Query q = Query.query(Criteria.where("_id").is(commentKey));
            Update dec = new Update().inc("likeCount", -1);
            mongo.updateFirst(q, dec, ActivityCommentDoc.class);

            // 3-2) 음수 방지
            Query fixNeg = Query.query(
                new Criteria().andOperator(
                    Criteria.where("_id").is(commentKey),
                    Criteria.where("likeCount").lt(0)
                )
            );
            Update zero = new Update().set("likeCount", 0);
            mongo.updateFirst(fixNeg, zero, ActivityCommentDoc.class);
        }
    }
}