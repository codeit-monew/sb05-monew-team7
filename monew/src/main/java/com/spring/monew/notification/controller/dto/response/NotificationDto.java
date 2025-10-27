package com.spring.monew.notification.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(name = "NotificationDto", description = "알림 단건 응답 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id", "createdAt", "updatedAt", "confirmed",
    "userId", "content", "resourceType", "resourceId"
})
public record NotificationDto(
    @Schema(description = "알림 ID", example = "70f35bd9-4191-8d1a-8bc6-123456789abc", format = "uuid")
    UUID id,

    @Schema(description = "생성 시각", example = "2025-10-23T04:22:14.468Z", format = "date-time")
    Instant createdAt,

    @Schema(description = "수정 시각", example = "2025-10-23T04:22:14.900Z", format = "date-time", nullable = true)
    Instant updatedAt,

    @Schema(description = "확인 여부", example = "false")
    boolean confirmed,

    @Schema(description = "수신자 사용자 ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", format = "uuid")
    UUID userId,

    @Schema(description = "알림 내용(최대 255자)", example = "[관심사명] 관련 기사가 3건 등록되었습니다.")
    String content,

    @Schema(description = "관련 리소스 타입", allowableValues = {"interest", "comment"}, nullable = true, example = "interest")
    String resourceType,

    @Schema(description = "관련 리소스 ID", example = "de9e6b6e-1e8e-4bf-98db-5f1f37a4e7a2", format = "uuid", nullable = true)
    UUID resourceId
) {}