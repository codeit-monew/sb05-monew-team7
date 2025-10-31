package com.spring.monew.batch.writer;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleWriter implements ItemWriter<Article> {

    private final ArticleRepository articleRepository;

    @Override
    public void write(Chunk<? extends Article> chunk) {
        // 기본 false: 원본 동작 100% 유지. 필요 시 -Dmonew.writer.optimize=true로
        boolean optimize = Boolean.parseBoolean(
            System.getProperty("monew.writer.optimize", "false")
        );
        if (optimize) {
            int saved = 0;
            int skipped = 0;

            Set<String> seenUrls = new LinkedHashSet<>();
            List<Article> candidates = new ArrayList<>();

            // 1) 후보 수집
            for (Article a : chunk) {
                String url = a.getSourceUrl();
                if (url == null || url.isBlank()) { skipped++; continue; }
                if (!seenUrls.add(url)) { skipped++; continue; }
                candidates.add(a);
            }

            // 2) DB 존재 URL 일괄 조회 (N+1 제거)
            Set<String> existing = seenUrls.isEmpty()
                ? Set.of()
                : articleRepository.findExistingSourceUrls(seenUrls);

            // 3) 실제 저장분을 관심사별로 집계
            Map<UUID, Integer> writerAgg = new HashMap<>();
            for (Article a : candidates) {
                String url = a.getSourceUrl();
                if (existing.contains(url)) { skipped++; continue; }

                articleRepository.save(a);
                saved++;

                // Article에 읽기전용 FK 필드가 있어야 함: getInterestId()
                UUID interestId = null;
                try {
                    interestId = a.getInterestId(); // LAZY 회피
                } catch (Exception ignore) { /* 안전 */ }

                if (interestId != null) {
                    writerAgg.merge(interestId, 1, Integer::sum);
                }
            }

            // 4) ExecutionContext("aggByInterest")에 누적
            var ctx = StepSynchronizationManager.getContext();
            if (ctx != null && ctx.getStepExecution() != null) {
                var ec = ctx.getStepExecution().getExecutionContext();
                @SuppressWarnings("unchecked")
                Map<UUID, Integer> total =
                    (Map<UUID, Integer>) ec.get("aggByInterest");
                if (total == null) {
                    total = new HashMap<>();
                    ec.put("aggByInterest", total);
                }
                for (var e : writerAgg.entrySet()) {
                    total.merge(e.getKey(), e.getValue(), Integer::sum);
                }
            }
            // 통계 로그
            log.info("청크 저장 완료: {} 개의 기사 {} 개 저장, {} 개 스킵", chunk.size(), saved, skipped);
            return;
        }
        for (Article article : chunk) {
            articleRepository.save(article);
        }
        log.info("청크 저장 완료: {} 개의 기사", chunk.size());
    }
}
