package com.spring.monew.article.batch.exception;

public class BatchJobExecutionNotFoundException extends RuntimeException {
    public BatchJobExecutionNotFoundException(String message) {
        super(message);
    }
}
