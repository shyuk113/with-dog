package com.example.withdog.walk.infrastructure;

import com.example.withdog.walk.domain.Walk;
import com.example.withdog.walk.domain.WalkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//도메인 포트 WalkRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class WalkRepositoryAdapter implements WalkRepository {

    private final WalkJpaRepository walkJpaRepository;

    @Override
    public Walk save(Walk walk) {
        return walkJpaRepository.save(walk);
    }

    @Override
    public Page<Walk> findByUserId(Pageable pageable, Long userId) {
        return walkJpaRepository.findByUserId(pageable, userId);
    }

    @Override
    public Optional<Walk> findByIdAndUserId(Long id, Long userId) {
        return walkJpaRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public Optional<Walk> findByUserIdAndEndedAtIsNull(Long userId) {
        return walkJpaRepository.findByUserIdAndEndedAtIsNull(userId);
    }

    @Override
    public List<Walk> findByUserIdAndEndedAtIsNotNullAndStartedAtBetween(Long userId, LocalDateTime start, LocalDateTime end) {
        return walkJpaRepository.findByUserIdAndEndedAtIsNotNullAndStartedAtBetween(userId, start, end);
    }
}
