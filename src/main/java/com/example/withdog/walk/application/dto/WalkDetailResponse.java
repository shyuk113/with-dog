package com.example.withdog.walk.application.dto;

import com.example.withdog.dog.application.dto.DogResponse;
import com.example.withdog.walk.domain.RoutePoint;
import com.example.withdog.walk.domain.Walk;

import java.time.LocalDateTime;
import java.util.List;

public record WalkDetailResponse(
        Long id,
        DogResponse dog,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        double distanceKm,
        long durationMinute,
        List<RoutePointResponse> routePoints
) {
    public static WalkDetailResponse of(Walk walk, List<RoutePoint> routePoints) {
        List<RoutePointResponse> sortedRoutePoints = routePoints.stream()
                .sorted((a, b) -> a.getCapturedAt().compareTo(b.getCapturedAt()))
                .map(RoutePointResponse::from)
                .toList();

        return new WalkDetailResponse(
                walk.getId(),
                DogResponse.from(walk.getDog()),
                walk.getStartedAt(),
                walk.getEndedAt(),
                walk.getDistanceKm() == null ? 0 : walk.getDistanceKm(),
                walk.getDurationMinutes(),
                sortedRoutePoints
        );
    }
}
