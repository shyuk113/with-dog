package com.example.withdog.region.infrastructure;

import com.example.withdog.region.domain.Region;

import java.util.Optional;

public interface RegionClient {

    //좌표 -> 시군구 변환 (변환 실패 시 empty)
    Optional<Region> findSigungu(double lat, double lon);
}
