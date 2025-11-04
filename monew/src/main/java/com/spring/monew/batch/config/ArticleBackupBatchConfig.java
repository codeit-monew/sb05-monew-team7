package com.spring.monew.batch.config;

import com.spring.monew.article.domain.Article;
import com.spring.monew.backup.dto.ArticleBackupDto;
import com.spring.monew.batch.processor.ArticleBackupProcessor;
import com.spring.monew.batch.writer.ArticleBackupWriter;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ArticleBackupBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final ArticleBackupProcessor articleBackupProcessor;
    private final ArticleBackupWriter articleBackupWriter;

    @Bean
    public Job articleBackupJob() {
        return new JobBuilder("articleBackupJob", jobRepository)
                .start(articleBackupStep())
                .build();
    }

    @Bean
    public Step articleBackupStep() {
        return new StepBuilder("articleBackupStep", jobRepository)
                .<Article, ArticleBackupDto>chunk(100, transactionManager)
                .reader(articleBackupReader())
                .processor(articleBackupProcessor)
                .writer(articleBackupWriter)
                .listener(articleBackupWriter)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<Article> articleBackupReader() {
        Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", yesterday);
        parameters.put("endDate", today);

        return new JpaPagingItemReaderBuilder<Article>()
                .name("articleBackupReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT a FROM Article a WHERE a.isDeleted = false AND a.createdAt >= :startDate AND a.createdAt < :endDate ORDER BY a.createdAt ASC")
                .pageSize(100)
                .parameterValues(parameters)
                .build();
    }
}
