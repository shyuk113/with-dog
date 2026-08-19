package com.example.withdog.recommend.domain;

import java.util.List;

public record RecommendResult(String courseName,
                              double distanceKm,
                              int durationMinutes,
                              String reason,
                              List<RouteStep> route) {
}
