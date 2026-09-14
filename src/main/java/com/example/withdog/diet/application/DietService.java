package com.example.withdog.diet.application;

import com.example.withdog.diet.application.dto.*;
import com.example.withdog.diet.domain.FoodItem;
import com.example.withdog.diet.domain.FoodLog;
import com.example.withdog.diet.domain.FoodType;
import com.example.withdog.diet.infrastructure.FoodItemRepository;
import com.example.withdog.diet.infrastructure.FoodLogRepository;
import com.example.withdog.diet.infrastructure.ocr.FoodOcrClient;
import com.example.withdog.diet.infrastructure.ocr.FoodOcrResult;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.global.infrastructure.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DietService {

    private static final String IMAGE_SUB_DIRECTORY = "diet";

    // 간식은 하루 총 급여 칼로리의 10%를 넘지 않아야 한다는 일반적인 반려동물 영양 가이드라인 (10% rule)
    private static final double TREAT_CALORIE_RATIO = 0.10;

    private static final Set<String> WEIGHT_LOSS_CONDITIONS = Set.of("비만", "과체중");
    private static final Set<String> WEIGHT_GAIN_CONDITIONS = Set.of("저체중", "영양실조");

    private final DogRepository dogRepository;
    private final FoodItemRepository foodItemRepository;
    private final FoodLogRepository foodLogRepository;
    private final FoodOcrClient foodOcrClient;
    private final ImageStorageService imageStorageService;

    //강아지 상태(체중/품종/지병) 기반 하루 권장 급여 칼로리 계산
    @Transactional(readOnly = true)
    public DietRecommendationResponse recommendDailyCalories(Long dogId, Long userId) {
        Dog dog = dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        return calculateRecommendation(dog);
    }

    private DietRecommendationResponse calculateRecommendation(Dog dog) {
        double weightKg = dog.getWeight();

        // RER(안정시 에너지 요구량) = 70 * (체중kg)^0.75 — 수의영양학에서 널리 쓰이는 표준 공식
        // 체중을 0.75제곱으로 스케일링하기 때문에, 소형견/대형견(품종) 차이가 이미 이 공식 안에 반영된다
        double rer = 70 * Math.pow(weightKg, 0.75);

        double activityFactor = 1.6; // 성견(중성화) 평균 활동계수
        String conditionNote = "특이 지병 없음";
        if (dog.getHealthConditions().stream().anyMatch(WEIGHT_LOSS_CONDITIONS::contains)) {
            activityFactor = 1.0; // 체중 감량이 필요한 경우 보수적으로 낮춤
            conditionNote = "비만/과체중 관리로 급여량을 보수적으로 산출";
        } else if (dog.getHealthConditions().stream().anyMatch(WEIGHT_GAIN_CONDITIONS::contains)) {
            activityFactor = 1.8; // 체중 증량이 필요한 경우 상향
            conditionNote = "저체중 관리로 급여량을 상향 산출";
        }

        double dailyCalories = rer * activityFactor;
        double treatCalorieLimit = dailyCalories * TREAT_CALORIE_RATIO;
        double mainFoodCalorieLimit = dailyCalories - treatCalorieLimit;

        String basis = String.format(
                "%s(%.1fkg) 기준 RER %.0fkcal × 활동계수 %.1f = 일일 %.0fkcal (%s). 간식은 전체의 10%%(%.0fkcal)를 넘지 않는 것을 권장합니다.",
                dog.getBreed(), weightKg, rer, activityFactor, dailyCalories, conditionNote, treatCalorieLimit
        );

        return new DietRecommendationResponse(weightKg, round(dailyCalories), round(treatCalorieLimit), round(mainFoodCalorieLimit), basis);
    }

    //사료/간식 상품 사진 스캔 -> 상품명/칼로리 정보 인식 및 기록
    @Transactional
    public FoodItemResponse scanFood(Long dogId, Long userId, FoodType type, MultipartFile image) {
        Dog dog = dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));

        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE);
        }

        String imageUrl = imageStorageService.store(image, IMAGE_SUB_DIRECTORY);
        FoodOcrResult ocrResult = foodOcrClient.extractFoodInfo(imageBytes, image.getOriginalFilename());

        FoodItem foodItem = FoodItem.createFromScan(dog, type, ocrResult.productName(), ocrResult.caloriesPerGram(), imageUrl, ocrResult.rawText());
        foodItemRepository.save(foodItem);
        return FoodItemResponse.from(foodItem);
    }

    //강아지가 먹고 있는 사료/간식 목록 조회
    @Transactional(readOnly = true)
    public List<FoodItemResponse> getFoodItems(Long dogId, Long userId) {
        dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        return foodItemRepository.findAllByDogIdOrderByCreatedAtDesc(dogId).stream()
                .map(FoodItemResponse::from)
                .toList();
    }

    //급여 기록 남기기
    @Transactional
    public FoodLogResponse logFeeding(Long dogId, Long userId, CreateFoodLogRequest request) {
        Dog dog = dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        FoodItem foodItem = foodItemRepository.findByIdAndDogId(request.foodItemId(), dogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOOD_ITEM_NOT_FOUND));

        LocalDateTime fedAt = request.fedAt() != null ? request.fedAt() : LocalDateTime.now();
        FoodLog foodLog = FoodLog.create(dog, foodItem, request.amountGrams(), fedAt);
        foodLogRepository.save(foodLog);
        return FoodLogResponse.from(foodLog);
    }

    //오늘 급여 기록을 토대로 배급량 조절 추천
    @Transactional(readOnly = true)
    public DietSummaryResponse getTodaySummary(Long dogId, Long userId) {
        Dog dog = dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        DietRecommendationResponse recommendation = calculateRecommendation(dog);

        LocalDate today = LocalDate.now();
        List<FoodLog> todayLogs = foodLogRepository.findByDogIdAndFedAtBetween(dogId, today.atStartOfDay(), today.plusDays(1).atStartOfDay());

        double caloriesGivenToday = todayLogs.stream().mapToDouble(FoodLog::calories).sum();
        double remainingCalories = recommendation.dailyCalories() - caloriesGivenToday;

        String adjustmentMessage = buildAdjustmentMessage(recommendation.dailyCalories(), caloriesGivenToday, remainingCalories);

        List<FoodLogResponse> logResponses = todayLogs.stream().map(FoodLogResponse::from).toList();

        return new DietSummaryResponse(today, recommendation, round(caloriesGivenToday), round(remainingCalories), adjustmentMessage, logResponses);
    }

    private String buildAdjustmentMessage(double dailyCalories, double caloriesGivenToday, double remainingCalories) {
        double ratio = caloriesGivenToday / dailyCalories;
        if (ratio > 1.1) {
            return String.format("오늘 급여량이 권장량보다 %.0fkcal 많습니다. 다음 급여량을 줄여주세요.", -remainingCalories);
        } else if (ratio < 0.7) {
            return String.format("오늘 급여량이 권장량보다 %.0fkcal 부족합니다. 남은 급여량을 늘려주세요.", remainingCalories);
        }
        return "오늘 급여량이 권장 범위 내에 있습니다.";
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
