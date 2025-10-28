package com.spring.monew.batch.exception;

public class BatchJobExecutionNotFoundException extends RuntimeException {
    public BatchJobExecutionNotFoundException(String message) {
        super(message);
    }
}
