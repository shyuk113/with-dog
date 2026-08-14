package com.example.withdog.recommend.application.dto;

import com.example.withdog.recommend.domain.RecommendResult;

public record RecommendResponse(String courseName, double distanceKm, int durationMinutes, String reason) {

    public static RecommendResponse from(RecommendResult recommendResult){
        return new RecommendResponse(recommendResult.courseName(), recommendResult.distanceKm(), recommendResult.durationMinutes(), recommendResult.reason());
    }
}
