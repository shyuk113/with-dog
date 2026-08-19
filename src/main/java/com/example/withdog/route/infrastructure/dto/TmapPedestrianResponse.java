package com.example.withdog.route.infrastructure.dto;

import tools.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmapPedestrianResponse(List<Feature> features) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Feature(Geometry geometry){}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(String type, JsonNode coordinates){}
}
