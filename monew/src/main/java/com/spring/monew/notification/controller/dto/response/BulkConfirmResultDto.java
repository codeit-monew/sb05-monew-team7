package com.spring.monew.notification.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "BulkConfirmResultDto", description = "전체 확인 결과")
@Builder
public record BulkConfirmResultDto(
    @Schema(description = "이번 호출로 확인 처리된 건수", example = "23") long updatedCount,

    @Schema(description = "호출 시점 기준 전체 미확인 0건 여부", example = "true") boolean allConfirmed,

    @Schema(description = "요청 사용자 ID", format = "uuid") UUID userId,

    @Schema(description = "처리 시각", format = "date-time", example = "2025-10-23T04:22:14.900Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    Instant processedAt
) {}
