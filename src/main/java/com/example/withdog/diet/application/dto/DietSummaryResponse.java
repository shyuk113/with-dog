package com.example.withdog.diet.application.dto;

import java.time.LocalDate;
import java.util.List;

public record DietSummaryResponse(
        LocalDate date,
        DietRecommendationResponse recommendation,
        double caloriesGivenToday,
        double remainingCalories,   // 음수면 초과 급여
        String adjustmentMessage,   // 배급량 조절 추천 메시지
        List<FoodLogResponse> todayLogs
) {
}
