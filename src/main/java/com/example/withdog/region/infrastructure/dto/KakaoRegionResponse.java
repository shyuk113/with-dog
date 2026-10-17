package com.example.withdog.region.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

//카카오 로컬 API 좌표 -> 행정구역 변환(coord2regioncode) 응답
@JsonIgnoreProperties(ignoreUnknown = true)
public record KakaoRegionResponse(List<Document> documents) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Document(
            @JsonProperty("region_type") String regionType, // B: 법정동, H: 행정동
            String code,
            @JsonProperty("region_1depth_name") String region1depthName,
            @JsonProperty("region_2depth_name") String region2depthName
    ) {}
}
