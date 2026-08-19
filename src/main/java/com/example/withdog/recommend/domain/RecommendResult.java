package com.example.withdog.recommend.domain;

import com.example.withdog.route.domain.RouteStep;

import java.util.List;

public record RecommendResult(String courseName,
                              double distanceKm,
                              int durationMinutes,
                              String reason,
                              List<RouteStep> route) {
}
