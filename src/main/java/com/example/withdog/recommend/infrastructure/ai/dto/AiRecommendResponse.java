package com.example.withdog.recommend.infrastructure.ai.dto;

import com.example.withdog.route.domain.RouteStep;

import java.util.List;

public record AiRecommendResponse(List<CourseDto> courses) {
    public record CourseDto(String courseName, double distanceKm, int durationMinutes, String reason, List<RouteStep> route){}

    public record RouteStepDto(double lat, double lon){}
}
