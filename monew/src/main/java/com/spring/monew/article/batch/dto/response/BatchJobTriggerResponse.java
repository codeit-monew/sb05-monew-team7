package com.spring.monew.article.batch.dto.response;

import org.springframework.batch.core.JobExecution;

import java.time.Instant;
import java.time.ZoneId;

public record BatchJobTriggerResponse(
    Long executionId,
    Long jobInstanceId,
    String jobName,
    String status,
    Instant triggerTime,
    String message
) {
    public static BatchJobTriggerResponse from(JobExecution execution) {
        return new BatchJobTriggerResponse(
            execution.getId(),
            execution.getJobInstance().getId(),
            execution.getJobInstance().getJobName(),
            execution.getStatus().name(),
            execution.getCreateTime().atZone(ZoneId.systemDefault()).toInstant(),
            "Batch job triggered successfully"
        );
    }
}
