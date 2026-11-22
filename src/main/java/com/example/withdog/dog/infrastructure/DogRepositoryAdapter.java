package com.example.withdog.dog.infrastructure;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.domain.DogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//도메인 포트 DogRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class DogRepositoryAdapter implements DogRepository {

    private final DogJpaRepository dogJpaRepository;

    @Override
    public Dog save(Dog dog) {
        return dogJpaRepository.save(dog);
    }

    @Override
    public void delete(Dog dog) {
        dogJpaRepository.delete(dog);
    }

    @Override
    public Optional<Dog> findByUserIdAndId(Long userId, Long id) {
        return dogJpaRepository.findByUserIdAndId(userId, id);
    }

    @Override
    public List<Dog> findAllByUserId(Long userId) {
        return dogJpaRepository.findAllByUserId(userId);
    }
}
