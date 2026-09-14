package com.example.withdog.diet.application.dto;

public record DietRecommendationResponse(
        double weightKg,
        double dailyCalories,       // 하루 총 권장 칼로리 (DER)
        double treatCalorieLimit,   // 간식으로 줄 수 있는 칼로리 상한 (DER의 10%)
        double mainFoodCalorieLimit,// 사료로 줄 수 있는 칼로리 (DER - 간식 상한)
        String basis                // 추천 산출 근거 설명
) {
}
