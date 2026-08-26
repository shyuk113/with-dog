package com.example.withdog.post.application.dto;

import com.example.withdog.post.domain.Post;

public record PostSummaryResponse(Long id, String title, Long userId) {

    public static PostSummaryResponse from(Post post) {
        return new PostSummaryResponse(post.getId(), post.getTitle(), post.getAuthor().getId());
    }
}
