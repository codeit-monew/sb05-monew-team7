package com.spring.monew.batch.dto.response;

import org.springframework.batch.core.StepExecution;

public record StepStatistics(
    String stepName,
    Long articlesRead,
    Long articlesFiltered,
    Long articlesWritten,
    Long readSkipCount,
    Long writeSkipCount,
    Long processSkipCount
) {
    public static StepStatistics from(StepExecution stepExecution) {
        if (stepExecution == null) {
            return new StepStatistics("N/A", 0L, 0L, 0L, 0L, 0L, 0L);
        }
        
        return new StepStatistics(
            stepExecution.getStepName(),
            stepExecution.getReadCount(),
            stepExecution.getFilterCount(),
            stepExecution.getWriteCount(),
            stepExecution.getReadSkipCount(),
            stepExecution.getWriteSkipCount(),
            stepExecution.getProcessSkipCount()
        );
    }
}
