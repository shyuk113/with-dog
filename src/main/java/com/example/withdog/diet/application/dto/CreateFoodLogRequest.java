package com.example.withdog.diet.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CreateFoodLogRequest(
        @NotNull
        Long foodItemId,
        @Positive
        double amountGrams,
        LocalDateTime fedAt // null이면 서버에서 현재 시각으로 기록
) {
}
