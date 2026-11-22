package com.example.withdog.dog.infrastructure;

import com.example.withdog.dog.domain.Dog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 DogRepository에 의존한다
public interface DogJpaRepository extends JpaRepository<Dog, Long> {

    Optional<Dog> findByUserIdAndId(Long userId, Long id);

    List<Dog> findAllByUserId(Long userId);
}
