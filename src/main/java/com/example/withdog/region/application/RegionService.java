package com.example.withdog.region.application;

import com.example.withdog.region.domain.Region;
import com.example.withdog.region.infrastructure.RegionClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionClient regionClient;

    //좌표가 없으면 변환하지 않는다 (nullable Double을 primitive로 언박싱하지 않도록 먼저 검사)
    public Optional<Region> resolve(Double lat, Double lon) {
        if (lat == null || lon == null) {
            return Optional.empty();
        }
        return regionClient.findSigungu(lat, lon);
    }
}
