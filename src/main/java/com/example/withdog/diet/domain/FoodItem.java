package com.example.withdog.diet.domain;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    @Enumerated(EnumType.STRING)
    private FoodType type;

    private String productName;

    // 상품 라벨에서 인식된 1g당 칼로리 (kcal/g). 인식 실패 시 null -> 급여량 추천 계산에서 제외됨
    private Double caloriesPerGram;

    private String imageUrl;

    @Lob
    private String rawText;

    @Builder
    private FoodItem(Dog dog, FoodType type, String productName, Double caloriesPerGram, String imageUrl, String rawText) {
        this.dog = dog;
        this.type = type;
        this.productName = productName;
        this.caloriesPerGram = caloriesPerGram;
        this.imageUrl = imageUrl;
        this.rawText = rawText;
    }

    public static FoodItem createFromScan(Dog dog, FoodType type, String productName, Double caloriesPerGram, String imageUrl, String rawText) {
        return FoodItem.builder()
                .dog(dog)
                .type(type)
                .productName(productName)
                .caloriesPerGram(caloriesPerGram)
                .imageUrl(imageUrl)
                .rawText(rawText)
                .build();
    }
}
