package com.example.withdog.route.infrastructure;

import com.example.withdog.route.domain.RouteStep;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("local")
public class MockRouteClient implements RouteClient {

    @Override
    public List<RouteStep> getWalkingRoute(List<RouteStep> waypoints) {
        return waypoints;
    }
}
