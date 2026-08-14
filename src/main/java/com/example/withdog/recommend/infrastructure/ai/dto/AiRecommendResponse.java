package com.example.withdog.recommend.infrastructure.ai.dto;

import java.util.List;

public record AiRecommendResponse(List<CourseDto> courses) {
    public record CourseDto(String courseName, double distanceKm, int durationMinutes, String reason){}
}
