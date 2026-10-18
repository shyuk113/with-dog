package com.example.withdog.feed.application.dto;

import com.example.withdog.post.application.dto.PostSummaryResponse;

public record HotPostResponse(PostSummaryResponse post, double score) {
}
