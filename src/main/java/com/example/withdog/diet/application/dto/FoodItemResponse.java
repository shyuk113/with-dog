package com.example.withdog.diet.application.dto;

import com.example.withdog.diet.domain.FoodItem;
import com.example.withdog.diet.domain.FoodType;

public record FoodItemResponse(
        Long id,
        Long dogId,
        FoodType type,
        String productName,
        Double caloriesPerGram,
        String imageUrl,
        String rawText
) {
    public static FoodItemResponse from(FoodItem foodItem) {
        return new FoodItemResponse(
                foodItem.getId(),
                foodItem.getDog().getId(),
                foodItem.getType(),
                foodItem.getProductName(),
                foodItem.getCaloriesPerGram(),
                foodItem.getImageUrl(),
                foodItem.getRawText()
        );
    }
}
