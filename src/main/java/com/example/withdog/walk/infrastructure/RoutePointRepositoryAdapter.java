package com.example.withdog.walk.infrastructure;

import com.example.withdog.walk.domain.RoutePoint;
import com.example.withdog.walk.domain.RoutePointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

//도메인 포트 RoutePointRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class RoutePointRepositoryAdapter implements RoutePointRepository {

    private final RoutePointJpaRepository routePointJpaRepository;

    @Override
    public List<RoutePoint> saveAll(List<RoutePoint> routePoints) {
        return routePointJpaRepository.saveAll(routePoints);
    }

    @Override
    public List<RoutePoint> findByWalkId(Long walkId) {
        return routePointJpaRepository.findByWalkId(walkId);
    }
}
