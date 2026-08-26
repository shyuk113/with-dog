package com.example.withdog.post.application.dto;

import com.example.withdog.post.domain.Post;

public record PostDetailResponse(Long id, String title, String content, Long userId) {

    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(post.getId(), post.getTitle(), post.getContent(), post.getAuthor().getId());
    }
}
