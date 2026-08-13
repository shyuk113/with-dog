package com.example.withdog.post.application.dto;

import com.example.withdog.post.domain.Post;

public record PostResponse(Long id, String title, String content, Long userId) {

    public static PostResponse from(Post post) {
        return new PostResponse(post.getId(), post.getTitle(), post.getContent(), post.getAuthor().getId());
    }
}
