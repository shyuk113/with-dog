package com.example.withdog.walk.application.dto;

import java.time.LocalDateTime;

public record RoutePointRequest(double lat, double lon, LocalDateTime capturedAt) {
}
