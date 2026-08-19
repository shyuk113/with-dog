package com.example.withdog.route.infrastructure;

import com.example.withdog.route.domain.RouteStep;
import com.example.withdog.route.infrastructure.dto.TmapPedestrianResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!local")
public class TmapRouteClient implements RouteClient {

    private final WebClient routeWebClient;

    @Value("${route.app-key}")
    private String appKey;

    @Override
    public List<RouteStep> getWalkingRoute(List<RouteStep> wayPoints) {
        if(wayPoints == null || wayPoints.size() < 2){
            return wayPoints == null ? List.of() : wayPoints;
        }

        RouteStep start = wayPoints.get(0);
        RouteStep end = wayPoints.get(wayPoints.size() - 1);

        Map<String, Object> body = new HashMap<>();
        body.put("startX", start.lon());
        body.put("startY", start.lat());
        body.put("endX", end.lon());
        body.put("endY", end.lat());
        body.put("startName", "출발");
        body.put("endName", "도착");
        if (wayPoints.size() > 2){
            body.put("passList",wayPoints.subList(1, wayPoints.size()-1).stream()
                    .map(w -> w.lon() + "," + w.lat())
                    .collect(Collectors.joining("_")));
        }

        try{
            TmapPedestrianResponse response = routeWebClient.post()
                    .uri("/tmap/routes/pedestrian?version=1")
                    .header("appKey", appKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(TmapPedestrianResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if(response == null || response.features() == null){
                return wayPoints;
            }

            List<RouteStep> route = new ArrayList<>();
            for(TmapPedestrianResponse.Feature feature : response.features()){
                if(!"LineString".equals(feature.geometry().type())) continue;
                for(JsonNode point : feature.geometry().coordinates()){
                    route.add(new RouteStep(point.get(1).asDouble(), point.get(0).asDouble()));
                }
            }
            return route.isEmpty() ? wayPoints : route;
        } catch(Exception e){
            log.error("Tmap 경로 조회 실패:{}", e.getMessage(),e);
            return wayPoints;
        }
    }




}
