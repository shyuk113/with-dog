package com.example.withdog.post.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

public record PostRouteRequest(
        @NotBlank
        String courseName,
        @PositiveOrZero
        double distanceKm,
        @PositiveOrZero
        int durationMinutes,
        @NotEmpty
        @Valid
        List<RouteStepRequest> route
) {
    public record RouteStepRequest(
            @NotNull
            Double lat,
            @NotNull
            Double lon
    ) {
    }
}
