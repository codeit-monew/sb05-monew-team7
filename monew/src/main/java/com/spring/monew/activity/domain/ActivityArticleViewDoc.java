package com.spring.monew.activity.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "activity_article_views")
@CompoundIndexes({
    // 커서/정렬
    @CompoundIndex(name = "idx_aav_user_last_viewed_desc", def = "{'user_id': 1, 'last_viewed_at': -1}"),
    // 유니크 제약
    @CompoundIndex(name = "uk_aav_user_article", def = "{'user_id': 1, 'article_id': 1}", unique = true)
})
public class ActivityArticleViewDoc {

  @Id
  private UUID id;  // view 이벤트 ID (또는 해시)

  @Indexed(name = "idx_aav_user")
  @Field("user_id")
  private UUID userId;

  @Field("article_id")
  private UUID articleId;

  @Field("source")
  private String source;

  @Field("source_url")
  private String sourceUrl;

  @Field("article_title")
  private String articleTitle;

  @Field("article_published_date")
  private Instant articlePublishedDate;

  @Field("article_summary")
  private String articleSummary;

  @Field("article_comment_count")
  private long articleCommentCount;

  @Field("article_view_count")
  private long articleViewCount;

  @Field("created_at")
  private Instant createdAt;      // 최초 조회

  @Field("last_viewed_at")
  private Instant lastViewedAt;   // 최신 조회

  @Field("view_count_by_user")
  private long viewCountByUser;   // 누적
}
