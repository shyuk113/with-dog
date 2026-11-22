package com.example.withdog.diet.infrastructure;

import com.example.withdog.diet.domain.FoodItem;
import com.example.withdog.diet.domain.FoodItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//도메인 포트 FoodItemRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class FoodItemRepositoryAdapter implements FoodItemRepository {

    private final FoodItemJpaRepository foodItemJpaRepository;

    @Override
    public FoodItem save(FoodItem foodItem) {
        return foodItemJpaRepository.save(foodItem);
    }

    @Override
    public List<FoodItem> findAllByDogIdOrderByCreatedAtDesc(Long dogId) {
        return foodItemJpaRepository.findAllByDogIdOrderByCreatedAtDesc(dogId);
    }

    @Override
    public Optional<FoodItem> findByIdAndDogId(Long id, Long dogId) {
        return foodItemJpaRepository.findByIdAndDogId(id, dogId);
    }
}
