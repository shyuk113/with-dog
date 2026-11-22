package com.example.withdog.diet.domain;

import java.util.List;
import java.util.Optional;

//FoodItem 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 FoodItemRepositoryAdapter가 담당한다
public interface FoodItemRepository {

    FoodItem save(FoodItem foodItem);

    List<FoodItem> findAllByDogIdOrderByCreatedAtDesc(Long dogId);

    Optional<FoodItem> findByIdAndDogId(Long id, Long dogId);
}
