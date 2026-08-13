package com.example.withdog.post.application.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePostRequest(
        @NotBlank
        String title,
        @NotBlank
        String content) {
}
