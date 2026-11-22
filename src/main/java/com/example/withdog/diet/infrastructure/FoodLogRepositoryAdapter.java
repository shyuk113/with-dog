package com.example.withdog.diet.infrastructure;

import com.example.withdog.diet.domain.FoodLog;
import com.example.withdog.diet.domain.FoodLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

//도메인 포트 FoodLogRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class FoodLogRepositoryAdapter implements FoodLogRepository {

    private final FoodLogJpaRepository foodLogJpaRepository;

    @Override
    public FoodLog save(FoodLog foodLog) {
        return foodLogJpaRepository.save(foodLog);
    }

    @Override
    public List<FoodLog> findByDogIdAndFedAtBetween(Long dogId, LocalDateTime start, LocalDateTime end) {
        return foodLogJpaRepository.findByDogIdAndFedAtBetween(dogId, start, end);
    }
}
