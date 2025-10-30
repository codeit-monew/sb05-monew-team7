package com.spring.monew.activity.domain;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator; // ✅ 이걸 사용
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@Getter
@Document(collection = "activity_article_views")
@CompoundIndexes({
    @CompoundIndex(name = "idx_aav_user_last_viewed_desc", def = "{'user_id': 1, 'last_viewed_at': -1}"),
    @CompoundIndex(name = "uk_aav_user_article", def = "{'user_id': 1, 'article_id': 1}", unique = true)
})
public class ActivityArticleViewDoc {

  @Id
  private final UUID id;

  @Indexed(name = "idx_aav_user")
  @Field("user_id")
  private final UUID userId;

  @Field("article_id")
  private final UUID articleId;

  @Field("source")
  private final String source;

  @Field("source_url")
  private final String sourceUrl;

  @Field("article_title")
  private final String articleTitle;

  @Field("article_published_date")
  private final Instant articlePublishedDate;

  @Field("article_summary")
  private final String articleSummary;

  @Field("article_comment_count")
  private final long articleCommentCount;

  @Field("article_view_count")
  private final long articleViewCount;

  @Field("created_at")
  private final Instant createdAt;

  @Field("last_viewed_at")
  private final Instant lastViewedAt;

  @Field("view_count_by_user")
  private final long viewCountByUser;

  // ✅ Spring Data가 사용할 생성자 (대체 어노테이션)
  @PersistenceCreator
  public ActivityArticleViewDoc(
      UUID id,
      UUID userId,
      UUID articleId,
      String source,
      String sourceUrl,
      String articleTitle,
      Instant articlePublishedDate,
      String articleSummary,
      long articleCommentCount,
      long articleViewCount,
      Instant createdAt,
      Instant lastViewedAt,
      long viewCountByUser
  ) {
    this.id = id;
    this.userId = userId;
    this.articleId = articleId;
    this.source = source;
    this.sourceUrl = sourceUrl;
    this.articleTitle = articleTitle;
    this.articlePublishedDate = articlePublishedDate;
    this.articleSummary = articleSummary;
    this.articleCommentCount = articleCommentCount;
    this.articleViewCount = articleViewCount;
    this.createdAt = createdAt;
    this.lastViewedAt = lastViewedAt;
    this.viewCountByUser = viewCountByUser;
  }

// --- 정적 팩토리 (불변 패턴 유지) ---

  /**
   * 최초 열람 이벤트 생성용
   */
  public static ActivityArticleViewDoc firstView(
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
    Instant when = viewedAt != null ? viewedAt : Instant.now();
    return new ActivityArticleViewDoc(
        viewEventId,
        userId,
        articleId,
        source,
        sourceUrl,
        articleTitleSnapshot,
        articlePublishDateSnapshot,
        articleSummarySnapshot,
        articleCommentCountSnapshot,
        articleViewCountSnapshot,
        when, // createdAt
        when, // lastViewedAt
        1L    // viewCountByUser
    );
  }

  // 스냅샷을 모두 지정해 복원/마이그레이션 시 사용할 수 있는 팩토리
  public static ActivityArticleViewDoc of(
      UUID id,
      UUID userId,
      UUID articleId,
      String source,
      String sourceUrl,
      String articleTitle,
      Instant articlePublishedDate,
      String articleSummary,
      long articleCommentCount,
      long articleViewCount,
      Instant createdAt,
      Instant lastViewedAt,
      long viewCountByUser
  ) {
    return new ActivityArticleViewDoc(
        id, userId, articleId, source, sourceUrl, articleTitle,
        articlePublishedDate, articleSummary, articleCommentCount, articleViewCount,
        createdAt, lastViewedAt, viewCountByUser
    );
  }

  // 재열람 시(스냅샷 값 갱신 포함). 카운트 +1, lastViewedAt 갱신
  public ActivityArticleViewDoc viewedAgain(
      Instant viewedAt,
      Long articleCommentCountSnapshot,
      Long articleViewCountSnapshot
  ) {
    Instant when = viewedAt != null ? viewedAt : Instant.now();
    long newCommentCount = articleCommentCountSnapshot != null ? articleCommentCountSnapshot
        : this.articleCommentCount;
    long newViewCount =
        articleViewCountSnapshot != null ? articleViewCountSnapshot : this.articleViewCount;

    return new ActivityArticleViewDoc(
        this.id,
        this.userId,
        this.articleId,
        this.source,
        this.sourceUrl,
        this.articleTitle,
        this.articlePublishedDate,
        this.articleSummary,
        newCommentCount,
        newViewCount,
        this.createdAt,
        when,                    // lastViewedAt 업데이트
        this.viewCountByUser + 1 // 누적 +1
    );
  }

  // 기사 메타 스냅샷 일부만 바뀐 경우(제목/요약/발행시각)
  public ActivityArticleViewDoc withArticleSnapshot(
      String articleTitle,
      String articleSummary,
      Instant articlePublishedDate
  ) {
    return new ActivityArticleViewDoc(
        this.id,
        this.userId,
        this.articleId,
        this.source,
        this.sourceUrl,
        articleTitle != null ? articleTitle : this.articleTitle,
        articlePublishedDate != null ? articlePublishedDate : this.articlePublishedDate,
        articleSummary != null ? articleSummary : this.articleSummary,
        this.articleCommentCount,
        this.articleViewCount,
        this.createdAt,
        this.lastViewedAt,
        this.viewCountByUser
    );
  }
}