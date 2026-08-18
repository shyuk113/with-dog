package com.example.withdog.walk.application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateWalkRequest(
        @NotNull
        Long dogId,
        @NotNull
        LocalDateTime startedAt) {
}
