package com.spring.monew.comment.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CommentRegisterRequest(
    @NotNull(message = "기사 ID는 필수입니다")
    UUID articleId,

    @NotNull(message = "사용자 ID는 필수입니다")
    UUID userId,

    @NotBlank(message = "댓글 내용은 필수입니다")
    String content
) {

}
