package com.spring.monew.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleBackupScheduler {

    private final JobLauncher jobLauncher;
    private final Job articleBackupJob;

    @Scheduled(cron = "0 0 2 * * *")
    public void runArticleBackup() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addDate("startTime", new Date())
                    .addString("triggerType", "SCHEDULED")
                    .addLong("triggerTime", System.currentTimeMillis())
                    .toJobParameters();

            log.info("기사 백업 배치 작업 시작");
            jobLauncher.run(articleBackupJob, params);
            log.info("기사 백업 배치 작업 완료");
        } catch (Exception e) {
            log.error("기사 백업 배치 작업 실행 실패", e);
        }
    }
}
