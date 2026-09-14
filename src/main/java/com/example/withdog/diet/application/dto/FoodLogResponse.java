package com.example.withdog.diet.application.dto;

import com.example.withdog.diet.domain.FoodLog;

import java.time.LocalDateTime;

public record FoodLogResponse(
        Long id,
        Long foodItemId,
        String productName,
        double amountGrams,
        double calories,
        LocalDateTime fedAt
) {
    public static FoodLogResponse from(FoodLog foodLog) {
        return new FoodLogResponse(
                foodLog.getId(),
                foodLog.getFoodItem().getId(),
                foodLog.getFoodItem().getProductName(),
                foodLog.getAmountGrams(),
                foodLog.calories(),
                foodLog.getFedAt()
        );
    }
}
