package com.example.withdog.diet.application;

import com.example.withdog.diet.application.dto.CreateFoodLogRequest;
import com.example.withdog.diet.application.dto.DietRecommendationResponse;
import com.example.withdog.diet.application.dto.DietSummaryResponse;
import com.example.withdog.diet.application.dto.FoodItemResponse;
import com.example.withdog.diet.domain.FoodType;
import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class DietServiceTest {

    @Autowired
    private DietService dietService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DogRepository dogRepository;

    private Long userId;
    private Long dogId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울"));
        Dog dog = dogRepository.save(Dog.createDogProfile("콩이", Breed.POODLE, LocalDate.of(2021, 3, 1), 10.0, user));
        userId = user.getId();
        dogId = dog.getId();
    }

    private MockMultipartFile foodImage() {
        return new MockMultipartFile("image", "snack.jpg", "image/jpeg", "fake-bytes".getBytes());
    }

    @Test
    void 체중_기반으로_하루_권장_급여_칼로리를_계산한다() {
        DietRecommendationResponse recommendation = dietService.recommendDailyCalories(dogId, userId);

        // RER = 70 * 10^0.75 ≈ 393.65, 기본 활동계수 1.6 -> 약 629.8kcal
        double expectedRer = 70 * Math.pow(10.0, 0.75);
        double expectedDaily = expectedRer * 1.6;

        assertThat(recommendation.weightKg()).isEqualTo(10.0);
        assertThat(recommendation.dailyCalories()).isCloseTo(expectedDaily, org.assertj.core.data.Offset.offset(0.5));
        assertThat(recommendation.treatCalorieLimit()).isCloseTo(expectedDaily * 0.10, org.assertj.core.data.Offset.offset(0.5));
        assertThat(recommendation.basis()).contains("POODLE");
    }

    @Test
    void 비만인_강아지는_더_보수적인_칼로리를_추천받는다() {
        Dog dog = dogRepository.findById(dogId).orElseThrow();
        dog.updateHealthConditions(List.of("비만"));

        DietRecommendationResponse obese = dietService.recommendDailyCalories(dogId, userId);

        double expectedRer = 70 * Math.pow(10.0, 0.75);
        double expectedDailyForObese = expectedRer * 1.0; // 비만 -> 활동계수 1.0

        assertThat(obese.dailyCalories()).isCloseTo(expectedDailyForObese, org.assertj.core.data.Offset.offset(0.5));
        assertThat(obese.basis()).contains("비만");
    }

    @Test
    void 사료_간식_상품_사진을_스캔하면_상품명과_칼로리_정보가_기록된다() {
        FoodItemResponse snack = dietService.scanFood(dogId, userId, FoodType.SNACK, foodImage());

        assertThat(snack.type()).isEqualTo(FoodType.SNACK);
        assertThat(snack.productName()).isEqualTo("맛있는 오리 저키");
        assertThat(snack.caloriesPerGram()).isEqualTo(3.5);
        assertThat(snack.imageUrl()).startsWith("/uploads/diet/");

        List<FoodItemResponse> items = dietService.getFoodItems(dogId, userId);
        assertThat(items).hasSize(1);
    }

    @Test
    void 급여_기록을_남기고_오늘_요약에서_칼로리가_집계된다() {
        FoodItemResponse snack = dietService.scanFood(dogId, userId, FoodType.SNACK, foodImage());

        dietService.logFeeding(dogId, userId, new CreateFoodLogRequest(snack.id(), 20.0, null)); // 20g * 3.5kcal/g = 70kcal

        DietSummaryResponse summary = dietService.getTodaySummary(dogId, userId);

        assertThat(summary.date()).isEqualTo(LocalDate.now());
        assertThat(summary.caloriesGivenToday()).isEqualTo(70.0);
        assertThat(summary.todayLogs()).hasSize(1);
        assertThat(summary.todayLogs().get(0).productName()).isEqualTo("맛있는 오리 저키");
    }

    @Test
    void 급여량이_권장량보다_많이_기록되면_줄이라는_조절_추천을_받는다() {
        FoodItemResponse food = dietService.scanFood(dogId, userId, FoodType.FOOD, foodImage());
        // 권장 칼로리(~630kcal)를 훌쩍 넘기도록 많이 급여 (300g * 3.5kcal/g = 1050kcal)
        dietService.logFeeding(dogId, userId, new CreateFoodLogRequest(food.id(), 300.0, null));

        DietSummaryResponse summary = dietService.getTodaySummary(dogId, userId);

        assertThat(summary.remainingCalories()).isNegative();
        assertThat(summary.adjustmentMessage()).contains("줄여주세요");
    }

    @Test
    void 급여_기록이_거의_없으면_늘리라는_조절_추천을_받는다() {
        DietSummaryResponse summary = dietService.getTodaySummary(dogId, userId);

        assertThat(summary.caloriesGivenToday()).isEqualTo(0);
        assertThat(summary.adjustmentMessage()).contains("늘려주세요");
    }

    @Test
    void 다른_강아지의_사료를_급여_기록으로_남길_수_없다() {
        User otherUser = userRepository.save(User.createUserLocal("타인", "other@example.com", "encoded-password", "부산"));
        Dog otherDog = dogRepository.save(Dog.createDogProfile("초코", Breed.BEAGLE, LocalDate.of(2020, 1, 1), 8.0, otherUser));

        FoodItemResponse foodOfOtherDog = dietService.scanFood(otherDog.getId(), otherUser.getId(), FoodType.FOOD, foodImage());

        assertThatThrownBy(() -> dietService.logFeeding(dogId, userId, new CreateFoodLogRequest(foodOfOtherDog.id(), 10.0, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FOOD_ITEM_NOT_FOUND);
    }
}
