package com.spring.monew.batch.processor;

import com.spring.monew.article.domain.Article;
import com.spring.monew.backup.dto.ArticleBackupDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupProcessor implements ItemProcessor<Article, ArticleBackupDto> {

    @Override
    public ArticleBackupDto process(Article article) throws Exception {
        try {
            ArticleBackupDto backupDto = ArticleBackupDto.from(article);
            log.debug("기사 {} 백업 준비 완료", article.getId());
            return backupDto;
        } catch (Exception e) {
            log.error("기사 {} 처리 실패: {}", article.getId(), e.getMessage(), e);
            throw e;
        }
    }
}
