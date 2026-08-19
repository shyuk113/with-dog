package com.example.withdog.recommend.application.dto;

import com.example.withdog.recommend.domain.RecommendResult;
import com.example.withdog.route.domain.RouteStep;

import java.util.List;

public record RecommendResponse(String courseName, double distanceKm, int durationMinutes, String reason, List<RouteStep> route) {

    public static RecommendResponse from(RecommendResult recommendResult){
        return new RecommendResponse(recommendResult.courseName(), recommendResult.distanceKm(), recommendResult.durationMinutes(), recommendResult.reason(), recommendResult.route());
    }
}
