package com.example.withdog.walk.application.dto;

import jakarta.validation.constraints.PositiveOrZero;

public record UpdateWalkRequest(
        @PositiveOrZero
        double distanceKm) {
}
