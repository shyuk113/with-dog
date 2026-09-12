package com.example.withdog.post.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class PostRoutePoint {

    private double lat;
    private double lon;

    protected PostRoutePoint() {
    }

    public PostRoutePoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
