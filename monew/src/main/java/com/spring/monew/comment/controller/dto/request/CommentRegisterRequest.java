package com.spring.monew.comment.controller.dto.request;

import java.util.UUID;

public record CommentRegisterRequest(
        UUID articleId,
        UUID userId,
        String content
) {}
