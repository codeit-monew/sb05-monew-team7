package com.spring.monew.batch.config;

import com.spring.monew.batch.processor.ArticleCandidateProcessor;
import com.spring.monew.batch.reader.ArticleCandidateReader;
import com.spring.monew.batch.writer.ArticleWriter;
import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.article.domain.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class NewsCollectionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ArticleCandidateReader articleCandidateReader;
    private final ArticleCandidateProcessor articleCandidateProcessor;
    private final ArticleWriter articleWriter;
    private final BatchSkipListener batchSkipListener;

    @Bean
    public Job newsCollectionJob() {
        return new JobBuilder("newsCollectionJob", jobRepository)
                .start(newsCollectionStep())
                .build();
    }

    @Bean
    public Step newsCollectionStep() {
        return new StepBuilder("newsCollectionStep", jobRepository)
                .<ArticleCandidate, Article>chunk(10, transactionManager)
                .reader(articleCandidateReader)
                .processor(articleCandidateProcessor)
                .writer(articleWriter)
                .listener(articleCandidateReader)
                .listener(articleCandidateProcessor)
                .listener(batchSkipListener)
                .faultTolerant()
                .skip(DataIntegrityViolationException.class)
                .skipLimit(100)
                .build();
    }
}
