package com.example.withdog.diet.infrastructure;

import com.example.withdog.diet.domain.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 FoodLogRepository에 의존한다
public interface FoodLogJpaRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByDogIdOrderByFedAtDesc(Long dogId);

    List<FoodLog> findByDogIdAndFedAtBetween(Long dogId, LocalDateTime start, LocalDateTime end);
}
