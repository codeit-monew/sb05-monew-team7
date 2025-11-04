package com.spring.monew.batch.dto.response;

import java.time.Instant;

public record CleanupTriggerResponse(
    String jobName,
    String status,
    Instant triggerTime,
    int successCount,
    int failureCount,
    String message
) {
    public static CleanupTriggerResponse success(String jobName, int successCount, int failureCount) {
        return new CleanupTriggerResponse(
            jobName,
            "COMPLETED",
            Instant.now(),
            successCount,
            failureCount,
            "Cleanup job triggered successfully"
        );
    }

    public static CleanupTriggerResponse failure(String jobName, String errorMessage) {
        return new CleanupTriggerResponse(
            jobName,
            "FAILED",
            Instant.now(),
            0,
            0,
            errorMessage
        );
    }
}
