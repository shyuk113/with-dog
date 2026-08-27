package com.example.withdog.walk.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutePoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double latitude;
    private double longitude;

    private LocalDateTime capturedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "walk_id", nullable = false)
    private Walk walk;

    @Builder
    private RoutePoint(double latitude, double longitude, LocalDateTime capturedAt, Walk walk) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.capturedAt = capturedAt;
        this.walk = walk;
    }

    public static RoutePoint createRoutePoint(double latitude, double longitude, LocalDateTime capturedAt, Walk walk) {
        return RoutePoint.builder()
                .latitude(latitude)
                .longitude(longitude)
                .capturedAt(capturedAt)
                .walk(walk)
                .build();
    }

}
