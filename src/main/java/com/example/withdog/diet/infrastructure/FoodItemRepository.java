package com.example.withdog.diet.infrastructure;

import com.example.withdog.diet.domain.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findAllByDogIdOrderByCreatedAtDesc(Long dogId);

    Optional<FoodItem> findByIdAndDogId(Long id, Long dogId);
}
