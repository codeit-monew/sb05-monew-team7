package com.spring.monew.batch.dto.response;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record BatchJobExecutionResponse(
    Long executionId,
    Long jobInstanceId,
    String jobName,
    String status,
    String exitCode,
    Instant startTime,
    Instant endTime,
    Long durationMillis,
    StepStatistics stepStatistics
) {
    public static BatchJobExecutionResponse from(JobExecution execution) {
        StepExecution stepExecution = execution.getStepExecutions()
            .stream()
            .findFirst()
            .orElse(null);
        
        return new BatchJobExecutionResponse(
            execution.getId(),
            execution.getJobInstance().getId(),
            execution.getJobInstance().getJobName(),
            execution.getStatus().name(),
            execution.getExitStatus().getExitCode(),
            toInstant(execution.getStartTime()),
            toInstant(execution.getEndTime()),
            calculateDuration(execution),
            StepStatistics.from(stepExecution)
        );
    }
    
    private static Instant toInstant(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static Long calculateDuration(JobExecution execution) {
        if (execution.getStartTime() == null || execution.getEndTime() == null) {
            return null;
        }
        Instant start = toInstant(execution.getStartTime());
        Instant end = toInstant(execution.getEndTime());
        return end.toEpochMilli() - start.toEpochMilli();
    }
}
