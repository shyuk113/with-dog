package com.example.withdog.post.application.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank
        String title,
        @NotBlank
        String content
        ) {
}
