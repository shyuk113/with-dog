package com.example.withdog.route.application;

import com.example.withdog.route.domain.RouteStep;
import com.example.withdog.route.infrastructure.RouteClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteClient routeClient;

    public List<RouteStep> getWalkingRoute(List<RouteStep> waypoints) {
        return routeClient.getWalkingRoute(waypoints);
    }
}
