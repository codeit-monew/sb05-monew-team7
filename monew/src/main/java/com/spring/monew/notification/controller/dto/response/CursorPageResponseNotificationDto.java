package com.spring.monew.notification.controller.dto.response;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.Instant;
import java.util.List;

@Schema(name = "CursorPageResponseNotification", description = "알림 목록 커서 페이지 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"content", "nextCursor", "nextAfter", "size", "totalElements", "hasNext"})
public record CursorPageResponseNotificationDto(
    @ArraySchema(arraySchema = @Schema(description = "알림 목록"))
    List<NotificationDto> content,

    @Schema(description = "다음 페이지 커서(Base64 URL-safe: 'createdAt|id')", nullable = true)
    String nextCursor,

    @Schema(description = "서버가 사용한 after 기준시각", format = "date-time", nullable = true)
    Instant nextAfter,

    @Schema(description = "이번 페이지 사이즈", example = "20")
    int size,

    @Schema(description = "총 개수(커서 페이징은 보통 미집계)", nullable = true)
    Long totalElements,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext
) {}
