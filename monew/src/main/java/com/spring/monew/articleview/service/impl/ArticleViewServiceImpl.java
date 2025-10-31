package com.spring.monew.articleview.service.impl;

import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.repository.ActivityArticleViewRepository;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.exception.ArticleNotFoundException;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.articleview.domain.ArticleView;
import com.spring.monew.articleview.repository.ArticleViewRepository;
import com.spring.monew.articleview.service.ArticleViewService;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleViewServiceImpl implements ArticleViewService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final ActivityArticleViewRepository activityArticleViewRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public void trackView(UUID articleId, UUID userId) {
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId));

        String redisKey = "article:view:" + articleId + ":" + userId;
        
        try {
            Boolean isNewView = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofHours(24));

            if (!Boolean.TRUE.equals(isNewView)) {
                return;
            }

            article.incrementViewCount();
            ArticleView view = ArticleView.of(article, userId);
            articleViewRepository.save(view);

            saveToMongoDB(article, userId, view.getId());
            
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed. View tracking blocked for articleId={}, userId={}", 
                articleId, userId, e);
            throw e;
        }
    }

    private void saveToMongoDB(Article article, UUID userId, UUID viewEventId) {
        try {
            ActivityArticleViewDoc existingDoc = activityArticleViewRepository
                .findByUserIdAndArticleId(userId, article.getId())
                .orElse(null);

            if (existingDoc != null) {
                existingDoc.setLastViewedAt(Instant.now());
                existingDoc.setCommentCount(article.getCommentCount());
                existingDoc.setViewCount(article.getViewCount());
                activityArticleViewRepository.save(existingDoc);
            } else {
                ActivityArticleViewDoc newDoc = new ActivityArticleViewDoc(
                    viewEventId.toString(),
                    userId,
                    article.getId(),
                    article.getSource().name(),
                    article.getSourceUrl(),
                    article.getTitle(),
                    article.getSummary(),
                    article.getCommentCount(),
                    article.getViewCount(),
                    article.getPublishDate(),
                    Instant.now(),
                    Instant.now()
                );
                activityArticleViewRepository.save(newDoc);
            }
        } catch (Exception e) {
            log.error("MongoDB save failed (graceful degradation). articleId={}, userId={}", 
                article.getId(), userId, e);
        }
    }
}
