package com.spring.monew.batch.service;

import com.spring.monew.batch.dto.response.BatchJobExecutionResponse;
import com.spring.monew.batch.dto.response.BatchJobTriggerResponse;
import com.spring.monew.batch.dto.response.CleanupTriggerResponse;
import com.spring.monew.batch.exception.BatchJobExecutionNotFoundException;
import com.spring.monew.batch.scheduler.ArticleCleanupScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchJobService {

    private final JobLauncher jobLauncher;
    private final Job newsCollectionJob;
    private final Job articleBackupJob;
    private final Job logBackupJob;
    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;
    private final ArticleCleanupScheduler articleCleanupScheduler;

    public BatchJobTriggerResponse triggerNewsCollectionJob() {
        log.info("newsCollectionJob 수동 실행 요청");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("triggerType", "MANUAL")
                .addLong("triggerTime", System.currentTimeMillis())
                .toJobParameters();

        try {
            JobExecution jobExecution = jobLauncher.run(newsCollectionJob, jobParameters);
            log.info("Job 실행 성공 - executionId: {}, status: {}",
                    jobExecution.getId(), jobExecution.getStatus());
            return BatchJobTriggerResponse.from(jobExecution);
        } catch (Exception e) {
            log.error("newsCollectionJob 실행 실패", e);
            throw new RuntimeException("배치 작업 실행 실패", e);
        }
    }

    public BatchJobTriggerResponse triggerArticleBackupJob() {
        log.info("articleBackupJob 수동 실행 요청");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("triggerType", "MANUAL")
                .addLong("triggerTime", System.currentTimeMillis())
                .toJobParameters();

        try {
            JobExecution jobExecution = jobLauncher.run(articleBackupJob, jobParameters);
            log.info("articleBackupJob 실행 성공 - executionId: {}, status: {}",
                    jobExecution.getId(), jobExecution.getStatus());
            return BatchJobTriggerResponse.from(jobExecution);
        } catch (Exception e) {
            log.error("articleBackupJob 실행 실패", e);
            throw new RuntimeException("배치 작업 실행 실패", e);
        }
    }

    public BatchJobTriggerResponse triggerLogBackupJob() {
        log.info("logBackupJob 수동 실행 요청");

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("triggerType", "MANUAL")
                .addLong("triggerTime", System.currentTimeMillis())
                .toJobParameters();

        try {
            JobExecution jobExecution = jobLauncher.run(logBackupJob, jobParameters);
            log.info("logBackupJob 실행 성공 - executionId: {}, status: {}",
                    jobExecution.getId(), jobExecution.getStatus());
            return BatchJobTriggerResponse.from(jobExecution);
        } catch (Exception e) {
            log.error("logBackupJob 실행 실패", e);
            throw new RuntimeException("배치 작업 실행 실패", e);
        }
    }

    public CleanupTriggerResponse triggerArticleCleanupJob() {
        log.info("articleCleanupJob 수동 실행 요청");

        try {
            int[] counts = articleCleanupScheduler.deleteSoftDeletedArticlesManual();
            log.info("articleCleanupJob 실행 완료");
            return CleanupTriggerResponse.success("articleCleanupJob", counts[0], counts[1]);
        } catch (Exception e) {
            log.error("articleCleanupJob 실행 실패", e);
            return CleanupTriggerResponse.failure("articleCleanupJob", e.getMessage());
        }
    }

    public BatchJobExecutionResponse getExecutionDetails(Long executionId) {
        log.info("executionId: {} 에 대한 실행 상세 정보 조회 중", executionId);

        JobExecution jobExecution = jobExplorer.getJobExecution(executionId);
        if (jobExecution == null) {
            log.warn("executionId: {} 에 해당하는 JobExecution을 찾을 수 없습니다", executionId);
            throw new BatchJobExecutionNotFoundException("JobExecution을 찾을 수 없습니다: " + executionId);
        }

        return BatchJobExecutionResponse.from(jobExecution);
    }

    public List<BatchJobExecutionResponse> listRecentExecutions(String triggerType, Integer limit) {
        int recordLimit = (limit != null && limit > 0) ? limit : 10;
        log.info("최근 실행 목록 조회 중 - triggerType: {}, limit: {}", triggerType, recordLimit);

        List<String> jobNames = jobExplorer.getJobNames().stream().toList();
        if (jobNames.isEmpty()) {
            log.info("JobExplorer에서 job 이름을 찾을 수 없습니다");
            return List.of();
        }

        return jobNames.stream()
                .flatMap(jobName -> jobExplorer.getJobInstances(jobName, 0, recordLimit).stream())
                .flatMap(jobInstance -> jobExplorer.getJobExecutions(jobInstance).stream())
                .filter(jobExecution -> {
                    if (triggerType == null || triggerType.isBlank()) {
                        return true;
                    }
                    String executionTriggerType = jobExecution.getJobParameters().getString("triggerType");
                    return triggerType.equalsIgnoreCase(executionTriggerType);
                })
                .sorted((e1, e2) -> e2.getCreateTime().compareTo(e1.getCreateTime()))
                .limit(recordLimit)
                .map(BatchJobExecutionResponse::from)
                .collect(Collectors.toList());
    }
}
