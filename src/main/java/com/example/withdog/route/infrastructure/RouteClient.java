package com.example.withdog.route.infrastructure;

import com.example.withdog.route.domain.RouteStep;

import java.util.List;

public interface RouteClient {

    List<RouteStep> getWalkingRoute(List<RouteStep> wayPoints);
}
