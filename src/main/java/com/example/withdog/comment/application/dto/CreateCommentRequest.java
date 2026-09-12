package com.example.withdog.comment.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank
        String content,
        Long parentId // 대댓글이면 원본 댓글 id, 최상위 댓글이면 null
        ) {
}
