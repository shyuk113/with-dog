package com.example.withdog.walk.application.dto;

import com.example.withdog.walk.domain.RoutePoint;

import java.time.LocalDateTime;

public record RoutePointResponse(double lat, double lon, LocalDateTime capturedAt) {

    public static RoutePointResponse from(RoutePoint routePoint) {
        return new RoutePointResponse(routePoint.getLatitude(), routePoint.getLongitude(), routePoint.getCapturedAt());
    }
}
