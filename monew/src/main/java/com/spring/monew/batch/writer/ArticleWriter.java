package com.spring.monew.batch.writer;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleWriter implements ItemWriter<Article> {

    private final ArticleRepository articleRepository;

    @Override
    public void write(Chunk<? extends Article> chunk) {

        int saved = 0;
        int skipped = 0;

        // 청크 내부 중복 제거(첫 등장만 저장 시도)
        Set<String> seenUrls = new LinkedHashSet<>();

        for (Article article : chunk) {
            String url = article.getSourceUrl();

            if (url != null && !url.isBlank()) {
                // 청크 내부 중복이면 스킵
                if (!seenUrls.add(url)) {
                    skipped++;
                    continue;
                }
                // DB에 이미 있으면 스킵
                if (articleRepository.existsBySourceUrl(url)) {
                    skipped++;
                    continue;
                }
            }
            articleRepository.save(article);
            saved++;
        }
        log.info("청크 저장 완료: {} 개의 기사", chunk.size());
    }
}
