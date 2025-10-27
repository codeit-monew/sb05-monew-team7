package com.spring.monew.notification.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "NotificationConfirmResponseDto", description = "알림 단건 확인 결과")
@Builder
public record NotificationConfirmResponseDto(
    @Schema(description = "알림 ID", format = "uuid") UUID id,

    @Schema(description = "현재 확인 상태", example = "true") boolean confirmed,

    @Schema(description = "확인 전 이미 확인되어 있었는지", example = "false") boolean alreadyConfirmed,

    @Schema(description = "요청 사용자 ID", format = "uuid") UUID userId,

    @Schema(description = "갱신 시각", format = "date-time", example = "2025-10-23T04:22:14.900Z")
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    Instant updatedAt
) {}