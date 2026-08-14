package com.example.withdog.recommend.domain;

public record RecommendResult(String courseName,
                              double distanceKm,
                              int durationMinutes,
                              String reason) {
}
