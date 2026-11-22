package com.example.withdog.diet.domain;

import java.time.LocalDateTime;
import java.util.List;

//FoodLog 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 FoodLogRepositoryAdapter가 담당한다
public interface FoodLogRepository {

    FoodLog save(FoodLog foodLog);

    List<FoodLog> findByDogIdAndFedAtBetween(Long dogId, LocalDateTime start, LocalDateTime end);
}
