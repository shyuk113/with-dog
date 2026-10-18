package com.example.withdog.feed.application.dto;

import java.util.List;

//동네 인기글 (최근 3일 반응을 날짜 가중치로 합산한 점수 높은 순)
public record HotFeedResponse(String regionCode, String regionName, List<HotPostResponse> posts) {
}
