package com.example.withdog.diet.infrastructure;

import com.example.withdog.diet.domain.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 FoodItemRepository에 의존한다
public interface FoodItemJpaRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findAllByDogIdOrderByCreatedAtDesc(Long dogId);

    Optional<FoodItem> findByIdAndDogId(Long id, Long dogId);
}
