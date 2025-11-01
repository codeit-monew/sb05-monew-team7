package com.spring.monew.batch.controller;

import com.spring.monew.batch.dto.response.BatchJobExecutionResponse;
import com.spring.monew.batch.dto.response.BatchJobTriggerResponse;
import com.spring.monew.batch.service.BatchJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Batch Job API", description = "뉴스 수집 배치 작업 트리거 및 모니터링 API")
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Profile({"dev", "staging", "local"})
@ConditionalOnProperty(name = "spring.batch.trigger.enabled", havingValue = "true")
public class BatchJobController {

    private final BatchJobService batchJobService;

    @Operation(
            summary = "뉴스 수집 배치 작업 수동 트리거",
            description = "newsCollectionJob을 수동으로 실행합니다. 스케줄러와 별도로 즉시 실행됩니다."
    )
    @PostMapping("/trigger")
    public ResponseEntity<BatchJobTriggerResponse> triggerNewsCollectionJob() {
        BatchJobTriggerResponse response = batchJobService.triggerNewsCollectionJob();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "배치 작업 실행 상세 조회",
            description = "특정 executionId의 배치 작업 실행 상세 정보를 조회합니다 (Step 통계 포함)."
    )
    @GetMapping("/executions/{executionId}")
    public ResponseEntity<BatchJobExecutionResponse> getExecutionDetails(
            @Parameter(description = "배치 작업 실행 ID", example = "1")
            @PathVariable Long executionId
    ) {
        BatchJobExecutionResponse response = batchJobService.getExecutionDetails(executionId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "최근 배치 작업 실행 목록 조회",
            description = "최근 배치 작업 실행 목록을 조회합니다. triggerType으로 필터링 가능합니다."
    )
    @GetMapping("/executions")
    public ResponseEntity<List<BatchJobExecutionResponse>> listRecentExecutions(
            @Parameter(description = "트리거 유형 필터 (MANUAL, SCHEDULED)", example = "MANUAL")
            @RequestParam(required = false) String triggerType,
            @Parameter(description = "조회할 최대 개수 (기본값: 10)", example = "10")
            @RequestParam(required = false) Integer limit
    ) {
        List<BatchJobExecutionResponse> responses = batchJobService.listRecentExecutions(triggerType, limit);
        return ResponseEntity.ok(responses);
    }
}
