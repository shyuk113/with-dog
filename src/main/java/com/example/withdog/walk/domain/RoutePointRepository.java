package com.example.withdog.walk.domain;

import java.util.List;

//RoutePoint 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 RoutePointRepositoryAdapter가 담당한다
public interface RoutePointRepository {

    List<RoutePoint> saveAll(List<RoutePoint> routePoints);

    List<RoutePoint> findByWalkId(Long walkId);
}
