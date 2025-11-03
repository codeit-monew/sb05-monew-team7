package com.spring.monew.activity.domain;

import com.spring.monew.article.domain.ArticleSource;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("activity_article_views")
@CompoundIndexes({
    @CompoundIndex(name = "uk_view_user_article", def = "{'user_id': 1, 'article_id': 1}", unique = true),
    @CompoundIndex(name = "ix_view_user_lastViewed", def = "{'user_id': 1, 'last_viewed_at': -1}")
})
public class ActivityArticleViewDoc {
    @Id
    private String id; // viewEventId
    @Field("user_id")
    private UUID userId;
    @Field("article_id")
    private UUID articleId;
    @Field("source")
    private ArticleSource source;
    @Field("source_url")
    private String sourceUrl;
    @Field("title")
    private String title;
    @Field("summary")
    private String summary;
    @Field("comment_count")
    private Long commentCount;
    @Field("view_count")
    private Long viewCount;
    @Field("publish_date")
    private Instant publishDate;
    @Field("created_at")
    private Instant createdAt;
    @Field("last_viewed_at")
    private Instant lastViewedAt;
}