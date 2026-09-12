package com.example.withdog.post.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank
        String title,
        @NotBlank
        String content,
        @Valid
        PostRouteRequest route // 산책 코스를 공유하지 않는 일반 게시글이면 null
        ) {
}
