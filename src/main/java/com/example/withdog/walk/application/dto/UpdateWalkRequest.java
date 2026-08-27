package com.example.withdog.walk.application.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record UpdateWalkRequest(
        @PositiveOrZero
        double distanceKm,
        List<RoutePointRequest> routePointRequest) {
}
