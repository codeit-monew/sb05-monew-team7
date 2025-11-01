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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("activity_article_views")
@CompoundIndexes({
        @CompoundIndex(name = "uk_view_user_article", def = "{'userId': 1, 'articleId': 1}", unique = true),
        @CompoundIndex(name = "ix_view_user_lastViewed", def = "{'userId': 1, 'lastViewedAt': -1}")
})
public class ActivityArticleViewDoc {
    @Id private String id;        // viewEventId (최초 삽입 시)
    private UUID userId;
    private UUID articleId;
    private ArticleSource source;
    private String sourceUrl;
    private String title;
    private String summary;
    private Long commentCount;
    private Long viewCount;
    private Instant publishDate;
    private Instant createdAt;    // 최초 본 시각
    private Instant lastViewedAt; // 최근 본 시각
}