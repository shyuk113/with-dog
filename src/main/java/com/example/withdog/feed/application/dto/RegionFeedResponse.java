package com.example.withdog.feed.application.dto;

import com.example.withdog.post.application.dto.PostSummaryResponse;

import java.util.List;

//nextCursor: 다음 페이지 요청 시 cursor로 그대로 전달 (마지막 페이지면 null)
public record RegionFeedResponse(
        String regionCode,
        String regionName,
        List<PostSummaryResponse> posts,
        Long nextCursor,
        boolean hasNext
) {
}
