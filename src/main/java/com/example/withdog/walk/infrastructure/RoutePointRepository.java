package com.example.withdog.walk.infrastructure;

import com.example.withdog.walk.domain.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {

    List<RoutePoint> findByWalkId(Long walkId);
}
