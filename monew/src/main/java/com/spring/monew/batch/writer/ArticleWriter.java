package com.spring.monew.batch.writer;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleWriter implements ItemWriter<Article> {

    private final ArticleRepository articleRepository;

    // 시스템 프로퍼티 대신 Spring 설정 주입 (기본 false)
    @Value("${monew.writer.optimize:false}")
    private boolean optimize;

    private static final String AGG_KEY = "aggByInterest";

    @Override
    public void write(Chunk<? extends Article> chunk) {

        // ===== 최적화 ON: 기존 네가 만든 최적화 경로만 개선 =====
        if (optimize) {
            int saved = 0;
            int skipped = 0;

            Set<String> seenUrls = new LinkedHashSet<>();
            List<Article> candidates = new ArrayList<>();

            // 1) 후보 수집 (가독성 좋은 블록 if)
            for (Article a : chunk) {
                String url = (a != null) ? a.getSourceUrl() : null;
                if (url == null || url.isBlank()) {
                    skipped++;
                    continue;
                }
                if (!seenUrls.add(url)) {
                    skipped++;
                    continue;
                }
                candidates.add(a);
            }

            // 2) DB 존재 URL 일괄 조회 (N+1 제거)
            Set<String> existing = seenUrls.isEmpty()
                ? Set.of()
                : articleRepository.findExistingSourceUrls(seenUrls);

            // 3) 실제 저장분을 관심사별로 집계(Map<UUID,Integer>)
            Map<UUID, Integer> writerAgg = new HashMap<>();
            for (Article a : candidates) {
                String url = a.getSourceUrl();
                if (existing.contains(url)) {
                    skipped++;
                    continue;
                }

                articleRepository.save(a);
                saved++;

                // 읽기 전용 FK 필드(Article.getInterestId) 우선 사용
                UUID interestId = null;
                try {
                    interestId = a.getInterestId();
                } catch (Exception ignore) { /* 안전장치 */ }
                if (interestId == null && a.getInterest() != null) {
                    // 프록시여도 getId()는 초기화 없이 안전
                    interestId = a.getInterest().getId();
                }
                if (interestId != null) {
                    writerAgg.merge(interestId, 1, Integer::sum);
                }
            }

            // 4) ExecutionContext("aggByInterest")에 누적 (타입 안전)
            var ctx = StepSynchronizationManager.getContext();
            if (ctx != null && ctx.getStepExecution() != null) {
                var ec = ctx.getStepExecution().getExecutionContext();

                Map<UUID, Integer> total = null;
                Object obj = ec.get(AGG_KEY);
                if (obj instanceof Map) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<UUID, Integer> casted = (Map<UUID, Integer>) obj;
                        total = casted;
                    } catch (ClassCastException e) {
                        log.warn("ExecutionContext '{}' 타입 불일치(Map<UUID,Integer> 기대). 새로 초기화합니다.", AGG_KEY, e);
                    }
                }
                if (total == null) {
                    total = new HashMap<>();
                    ec.put(AGG_KEY, total);
                }
                for (var e : writerAgg.entrySet()) {
                    total.merge(e.getKey(), e.getValue(), Integer::sum);
                }
            }

            log.info("청크 저장 완료: {} 개의 기사 {} 개 저장, {} 개 스킵 (optimize=ON)",
                chunk.size(), saved, skipped);
            return;
        }
        for (Article article : chunk) {
            articleRepository.save(article);
        }
        log.info("청크 저장 완료: {} 개의 기사", chunk.size());
    }
}