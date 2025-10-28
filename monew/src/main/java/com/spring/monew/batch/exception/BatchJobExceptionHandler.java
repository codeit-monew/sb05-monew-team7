package com.spring.monew.batch.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice(basePackages = "com.spring.monew.article.batch.controller")
public class BatchJobExceptionHandler {

    @ExceptionHandler(BatchJobExecutionNotFoundException.class)
    public ProblemDetail handleBatchJobExecutionNotFound(BatchJobExecutionNotFoundException ex) {
        log.warn("배치 작업 실행을 찾을 수 없습니다: {}", ex.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problemDetail.setTitle("배치 작업 실행을 찾을 수 없음");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(RuntimeException.class)
    public ProblemDetail handleRuntimeException(RuntimeException ex) {
        log.error("배치 작업 처리 중 예상치 못한 오류가 발생했습니다", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "배치 작업 처리 중 예상치 못한 오류가 발생했습니다"
        );
        problemDetail.setTitle("내부 서버 오류");
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
