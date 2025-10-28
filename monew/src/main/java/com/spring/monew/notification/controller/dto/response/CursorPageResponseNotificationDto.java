package com.spring.monew.notification.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

//알림 목록 커서 페이지 응답 DTO.
@Schema(name = "CursorPageResponseNotification", description = "알림 목록 커서 페이지 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "content", "nextCursor", "nextAfter", "size", "totalElements", "hasNext" })
public record CursorPageResponseNotificationDto(

    @ArraySchema(arraySchema = @Schema(description = "알림 목록"))
    List<NotificationDto> content,

    @Schema(
        description = "다음 페이지 커서(Base64 URL-safe). 포맷: 'createdAt|id' (정렬: createdAt DESC, id DESC).",
        example = "MjAyNS0xMC0yM1QwNDoyMjoxNC40NjhafDcwZjM1YmQ5LTQxOTEtOGQxYS04YmM2LTEyMzQ1Njc4OWFiYw==",
        nullable = true
    )
    String nextCursor,

    @Schema(
        description = "서버가 이번 조회에 사용한 after 기준 시각",
        example = "2025-10-23T04:22:14.468Z",
        format = "date-time",
        nullable = true
    )
    Instant nextAfter,

    @Schema(description = "이번 페이지 항목 수", example = "20")
    int size,

    @Schema(description = "총 개수(커서 페이징에서는 일반적으로 집계하지 않음)", nullable = true, example = "0")
    Long totalElements,

    @Schema(description = "다음 페이지 존재 여부", example = "true")
    boolean hasNext
) {}