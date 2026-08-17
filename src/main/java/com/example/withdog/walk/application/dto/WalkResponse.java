package com.example.withdog.walk.application.dto;

import com.example.withdog.walk.domain.Walk;

import java.time.LocalDateTime;

public record WalkResponse(Long id, Long dogId, LocalDateTime startedAt, LocalDateTime endedAt, double distanceKm) {

    public static WalkResponse from(Walk walkHistory) {
        return new WalkResponse(walkHistory.getId(), walkHistory.getDog().getId(), walkHistory.getStartedAt(), walkHistory.getEndedAt(), walkHistory.getDistanceKm());
    }
}
