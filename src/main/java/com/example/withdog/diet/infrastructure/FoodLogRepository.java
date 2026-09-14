package com.example.withdog.diet.infrastructure;

import com.example.withdog.diet.domain.FoodLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface FoodLogRepository extends JpaRepository<FoodLog, Long> {

    List<FoodLog> findByDogIdOrderByFedAtDesc(Long dogId);

    List<FoodLog> findByDogIdAndFedAtBetween(Long dogId, LocalDateTime start, LocalDateTime end);
}
