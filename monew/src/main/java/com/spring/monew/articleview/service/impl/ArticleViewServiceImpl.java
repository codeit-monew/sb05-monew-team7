package com.spring.monew.articleview.service.impl;

import com.spring.monew.activity.domain.ActivityArticleViewDoc;
import com.spring.monew.activity.repository.ActivityArticleViewRepository;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.exception.ArticleNotFoundException;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
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
    public ArticleViewDto trackView(UUID articleId, UUID userId) {
        log.info("trackView called for articleId={}, userId={}", articleId, userId);
        
        Article article = articleRepository.findById(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId));

        String redisKey = "article:view:" + articleId + ":" + userId;
        log.info("Redis key: {}", redisKey);
        
        ArticleView view;
        Instant createdAt;
        
        try {
            Boolean isNewView = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", Duration.ofHours(24));
            log.info("Redis setIfAbsent returned: {} (class: {})", 
                isNewView, isNewView != null ? isNewView.getClass().getName() : "null");
            
            String checkValue = redisTemplate.opsForValue().get(redisKey);
            log.info("Redis GET after setIfAbsent: {}", checkValue);

            if (!Boolean.TRUE.equals(isNewView)) {
                log.info("Not a new view (isNewView={}), finding existing view", isNewView);
                Instant twentyFourHoursAgo = Instant.now().minusSeconds(24 * 60 * 60);
                view = articleViewRepository
                    .findTopByArticleIdAndUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                        articleId, userId, twentyFourHoursAgo
                    )
                    .orElseGet(() -> {
                        log.warn("Redis said duplicate but no recent view found in DB, creating new one without incrementing count");
                        ArticleView newView = ArticleView.of(article, userId);
                        ArticleView saved = articleViewRepository.save(newView);
                        saveToMongoDB(article, userId, saved.getId());
                        return saved;
                    });
                createdAt = view.getCreatedAt();
            } else {
                log.info("New view detected, incrementing view count and saving");
                article.incrementViewCount();
                view = ArticleView.of(article, userId);
                view = articleViewRepository.save(view);
                createdAt = view.getCreatedAt();
                saveToMongoDB(article, userId, view.getId());
            }
            
        } catch (RedisConnectionFailureException e) {
            log.error("Redis connection failed. Proceeding with DB fallback for articleId={}, userId={}", 
                articleId, userId, e);
            
            Instant twentyFourHoursAgo = Instant.now().minusSeconds(24 * 60 * 60);
            view = articleViewRepository
                .findTopByArticleIdAndUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
                    articleId, userId, twentyFourHoursAgo
                )
                .orElseGet(() -> {
                    log.info("No recent view found in DB, creating new view with count increment");
                    article.incrementViewCount();
                    ArticleView newView = ArticleView.of(article, userId);
                    ArticleView saved = articleViewRepository.save(newView);
                    saveToMongoDB(article, userId, saved.getId());
                    return saved;
                });
            createdAt = view.getCreatedAt();
        }

        return new ArticleViewDto(
            view.getId(),
            userId,
            createdAt,
            article.getId(),
            article.getSource(),
            article.getSourceUrl(),
            article.getTitle(),
            article.getPublishDate(),
            article.getSummary(),
            article.getCommentCount(),
            article.getViewCount()
        );
    }

    private void saveToMongoDB(Article article, UUID userId, UUID viewEventId) {
        log.info("saveToMongoDB called for articleId={}, userId={}, viewEventId={}", 
            article.getId(), userId, viewEventId);
        try {
            ActivityArticleViewDoc existingDoc = activityArticleViewRepository
                .findByUserIdAndArticleId(userId, article.getId())
                .orElse(null);

            if (existingDoc != null) {
                log.info("Updating existing MongoDB doc: {}", existingDoc.getId());
                existingDoc.setLastViewedAt(Instant.now());
                existingDoc.setCommentCount(article.getCommentCount());
                existingDoc.setViewCount(article.getViewCount());
                ActivityArticleViewDoc saved = activityArticleViewRepository.save(existingDoc);
                log.info("MongoDB doc updated: {}", saved.getId());
            } else {
                log.info("Creating new MongoDB doc");
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
                ActivityArticleViewDoc saved = activityArticleViewRepository.save(newDoc);
                log.info("MongoDB doc created: {}", saved.getId());
            }
        } catch (Exception e) {
            log.error("MongoDB save failed (graceful degradation). articleId={}, userId={}", 
                article.getId(), userId, e);
        }
    }
}
