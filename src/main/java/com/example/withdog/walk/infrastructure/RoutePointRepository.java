package com.example.withdog.walk.infrastructure;

import com.example.withdog.walk.domain.RoutePoint;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RoutePointRepository extends JpaRepository<RoutePoint, Long> {
}
