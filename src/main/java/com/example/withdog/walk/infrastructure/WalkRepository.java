package com.example.withdog.walk.infrastructure;

import com.example.withdog.walk.domain.Walk;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WalkRepository extends JpaRepository<Walk, Long> {

    Page<Walk> findByUserId(Pageable pageable, Long userId);

    Optional<Walk> findByIdAndUserId(Long id, Long userId);

    Optional<Walk> findByUserIdAndEndedAtIsNull(Long userId);

    List<Walk> findByUserIdAndEndedAtIsNotNullAndStartedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
