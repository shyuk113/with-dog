package com.example.withdog.walk.infrastructure;

import com.example.withdog.walk.domain.Walk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 WalkRepository에 의존한다
public interface WalkJpaRepository extends JpaRepository<Walk, Long> {

    Page<Walk> findByUserId(Pageable pageable, Long userId);

    Optional<Walk> findByIdAndUserId(Long id, Long userId);

    Optional<Walk> findByUserIdAndEndedAtIsNull(Long userId);

    List<Walk> findByUserIdAndEndedAtIsNotNullAndStartedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
