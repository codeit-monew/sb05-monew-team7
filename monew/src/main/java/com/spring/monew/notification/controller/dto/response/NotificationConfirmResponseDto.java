package com.spring.monew.notification.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "NotificationConfirmResponse", description = "단건 알림 확인 처리 결과")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"notificationId", "confirmed", "alreadyConfirmed", "userId", "updatedAt"})
public record NotificationConfirmResponseDto(
    @Schema(description = "알림 ID", example = "70f35bd9-4191-8d1a-8bc6-123456789abc", format = "uuid")
    UUID notificationId,

    @Schema(description = "확인 상태(처리 후 상태)", example = "true")
    boolean confirmed,

    @Schema(description = "이미 확인된 알림이었는지 여부(처리 전 상태)", example = "false")
    boolean alreadyConfirmed,

    @Schema(description = "요청 사용자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", format = "uuid")
    UUID userId,

    @Schema(description = "갱신 시각", example = "2025-10-23T04:22:14.900Z", format = "date-time")
    Instant updatedAt
) {
}