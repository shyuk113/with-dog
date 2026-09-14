package com.example.withdog.diet.domain;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id", nullable = false)
    private FoodItem foodItem;

    private double amountGrams;

    private LocalDateTime fedAt;

    @Builder
    private FoodLog(Dog dog, FoodItem foodItem, double amountGrams, LocalDateTime fedAt) {
        this.dog = dog;
        this.foodItem = foodItem;
        this.amountGrams = amountGrams;
        this.fedAt = fedAt;
    }

    public static FoodLog create(Dog dog, FoodItem foodItem, double amountGrams, LocalDateTime fedAt) {
        return FoodLog.builder()
                .dog(dog)
                .foodItem(foodItem)
                .amountGrams(amountGrams)
                .fedAt(fedAt)
                .build();
    }

    // 이 급여 기록의 칼로리 (상품의 kcal/g을 못 읽었으면 0으로 취급)
    public double calories() {
        Double caloriesPerGram = foodItem.getCaloriesPerGram();
        return caloriesPerGram == null ? 0 : amountGrams * caloriesPerGram;
    }
}
