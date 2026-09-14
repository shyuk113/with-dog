package com.example.withdog.diet.presentation;

import com.example.withdog.diet.application.DietService;
import com.example.withdog.diet.application.dto.CreateFoodLogRequest;
import com.example.withdog.diet.application.dto.DietRecommendationResponse;
import com.example.withdog.diet.application.dto.DietSummaryResponse;
import com.example.withdog.diet.application.dto.FoodItemResponse;
import com.example.withdog.diet.application.dto.FoodLogResponse;
import com.example.withdog.diet.domain.FoodType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/dogs/{dogId}/diet")
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    //체중/품종/지병 기반 하루 권장 급여 칼로리
    @GetMapping("/recommendation")
    public ResponseEntity<DietRecommendationResponse> getRecommendation(@PathVariable Long dogId, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(dietService.recommendDailyCalories(dogId, userId));
    }

    //사료/간식 상품 사진 스캔 -> 기록
    @PostMapping(value = "/foods/scan", consumes = "multipart/form-data")
    public ResponseEntity<FoodItemResponse> scanFood(@PathVariable Long dogId,
                                                       @RequestParam FoodType type,
                                                       @RequestParam("image") MultipartFile image,
                                                       @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dietService.scanFood(dogId, userId, type, image));
    }

    //먹고 있는 사료/간식 목록
    @GetMapping("/foods")
    public ResponseEntity<List<FoodItemResponse>> getFoodItems(@PathVariable Long dogId, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(dietService.getFoodItems(dogId, userId));
    }

    //급여 기록 남기기
    @PostMapping("/logs")
    public ResponseEntity<FoodLogResponse> logFeeding(@PathVariable Long dogId,
                                                        @Valid @RequestBody CreateFoodLogRequest request,
                                                        @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dietService.logFeeding(dogId, userId, request));
    }

    //오늘 급여 기록 + 배급량 조절 추천
    @GetMapping("/summary/today")
    public ResponseEntity<DietSummaryResponse> getTodaySummary(@PathVariable Long dogId, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(dietService.getTodaySummary(dogId, userId));
    }
}
