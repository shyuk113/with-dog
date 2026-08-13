package com.example.withdog.dog.infrastructure;

import com.example.withdog.dog.domain.Dog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DogRepository extends JpaRepository<Dog, Long> {

    Optional<Dog> findByUserIdAndId(Long userId, Long id);
}
