package com.example.withdog.region.infrastructure;

import com.example.withdog.region.domain.Region;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

//로컬/테스트용: 외부 API 없이 좌표 범위로 대략적인 시군구를 돌려준다
@Component
@Profile("local")
public class MockRegionClient implements RegionClient {

    static final Region BUSAN_HAEUNDAE = new Region("26350", "부산광역시 해운대구");
    static final Region SEOUL_MAPO = new Region("11440", "서울특별시 마포구");
    static final Region SEOUL_GANGNAM = new Region("11680", "서울특별시 강남구");

    @Override
    public Optional<Region> findSigungu(double lat, double lon) {
        if (lat < 36.0) {
            return Optional.of(BUSAN_HAEUNDAE);
        }
        return Optional.of(lon < 127.0 ? SEOUL_MAPO : SEOUL_GANGNAM);
    }
}
