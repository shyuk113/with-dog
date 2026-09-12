package com.example.withdog.walk.application.dto;

import com.example.withdog.walk.domain.Walk;

import java.time.LocalDateTime;

public record WalkResponse(Long id, Long dogId, LocalDateTime startedAt, LocalDateTime endedAt, double distanceKm, long durationMinute) {

    public static WalkResponse from(Walk walkHistory) {
        double distanceKm = walkHistory.getDistanceKm() == null ? 0 : walkHistory.getDistanceKm();
        return new WalkResponse(walkHistory.getId(), walkHistory.getDog().getId(), walkHistory.getStartedAt(), walkHistory.getEndedAt(), distanceKm, walkHistory.getDurationMinutes());
    }
}
