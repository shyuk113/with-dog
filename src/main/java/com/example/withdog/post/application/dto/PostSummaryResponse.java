package com.example.withdog.post.application.dto;

import com.example.withdog.post.domain.Post;

public record PostSummaryResponse(
        Long id,
        String title,
        Long userId,
        String imageUrl,
        boolean hasRoute,
        long likeCount,
        boolean likedByMe
) {
    public static PostSummaryResponse of(Post post, long likeCount, boolean likedByMe) {
        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getAuthor().getId(),
                post.getImageUrl(),
                post.hasRoute(),
                likeCount,
                likedByMe
        );
    }
}
