package com.spring.monew.batch.writer;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleWriter implements ItemWriter<Article> {

    private final ArticleRepository articleRepository;

    @Override
    public void write(Chunk<? extends Article> chunk) {
        for (Article article : chunk) {
            articleRepository.save(article);
        }
        log.info("청크 저장 완료: {} 개의 기사", chunk.size());
    }
}
