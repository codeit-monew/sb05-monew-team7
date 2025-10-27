package com.spring.monew.notification.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "BulkConfirmResult", description = "전체 알림 확인 처리 결과")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"updatedCount", "allConfirmed", "userId", "processedAt"})
public record BulkConfirmResultDto(
    @Schema(description = "이번 호출로 확인 처리된 건수", example = "23")
    long updatedCount,

    @Schema(description = "처리 후 미확인 알림이 남아있지 않으면 true", example = "true")
    boolean allConfirmed,

    @Schema(description = "요청 사용자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", format = "uuid")
    UUID userId,

    @Schema(description = "처리 시각", example = "2025-10-23T04:22:14.900Z", format = "date-time")
    Instant processedAt
) {}