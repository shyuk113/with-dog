package com.example.withdog.walk.infrastructure;

import com.example.withdog.walk.domain.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 RoutePointRepository에 의존한다
public interface RoutePointJpaRepository extends JpaRepository<RoutePoint, Long> {

    List<RoutePoint> findByWalkId(Long walkId);
}
