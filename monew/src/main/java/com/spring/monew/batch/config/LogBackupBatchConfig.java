package com.spring.monew.batch.config;

import com.spring.monew.backup.service.LogBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LogBackupBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final LogBackupService logBackupService;

    @Bean
    public Job logBackupJob() {
        return new JobBuilder("logBackupJob", jobRepository)
                .start(logBackupStep())
                .build();
    }

    @Bean
    public Step logBackupStep() {
        return new StepBuilder("logBackupStep", jobRepository)
                .tasklet(logBackupTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet logBackupTasklet() {
        return (contribution, chunkContext) -> {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            
            log.info("로그 백업 시작: {}", yesterday);
            
            try {
                logBackupService.uploadLogFile(yesterday);
                log.info("로그 백업 성공: {}", yesterday);
            } catch (Exception e) {
                log.error("로그 백업 실패: {}", yesterday, e);
                throw e;
            }
            
            return RepeatStatus.FINISHED;
        };
    }
}
