package com.example.withdog.walk.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Walk 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 WalkRepositoryAdapter가 담당한다
public interface WalkRepository {

    Walk save(Walk walk);

    Page<Walk> findByUserId(Pageable pageable, Long userId);

    Optional<Walk> findByIdAndUserId(Long id, Long userId);

    //진행 중인(종료되지 않은) 산책
    Optional<Walk> findByUserIdAndEndedAtIsNull(Long userId);

    List<Walk> findByUserIdAndEndedAtIsNotNullAndStartedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
