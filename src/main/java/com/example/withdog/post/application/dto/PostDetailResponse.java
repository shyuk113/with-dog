package com.example.withdog.post.application.dto;

import com.example.withdog.post.domain.Post;
import com.example.withdog.route.domain.RouteStep;

import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        Long userId,
        String imageUrl,
        String courseName,
        Double distanceKm,
        Integer durationMinutes,
        List<RouteStep> route,
        long likeCount,
        boolean likedByMe
) {
    public static PostDetailResponse of(Post post, long likeCount, boolean likedByMe) {
        List<RouteStep> route = post.getRoute().stream()
                .map(p -> new RouteStep(p.getLat(), p.getLon()))
                .toList();

        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthor().getId(),
                post.getImageUrl(),
                post.getCourseName(),
                post.getDistanceKm(),
                post.getDurationMinutes(),
                route,
                likeCount,
                likedByMe
        );
    }
}
