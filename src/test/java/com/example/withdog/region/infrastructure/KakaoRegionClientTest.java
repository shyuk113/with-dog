package com.example.withdog.region.infrastructure;

import com.example.withdog.region.domain.Region;
import com.example.withdog.region.infrastructure.dto.KakaoRegionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoRegionClientTest {

    @Test
    void 법정동_결과를_우선으로_시군구를_만든다() {
        KakaoRegionResponse response = new KakaoRegionResponse(List.of(
                new KakaoRegionResponse.Document("H", "1168064000", "서울특별시", "강남구"),
                new KakaoRegionResponse.Document("B", "1168010100", "서울특별시", "강남구")
        ));

        Optional<Region> region = KakaoRegionClient.toRegion(response);

        assertThat(region).contains(new Region("11680", "서울특별시 강남구"));
    }

    @Test
    void 일반구가_있는_시는_시와_구를_함께_이름으로_쓴다() {
        KakaoRegionResponse response = new KakaoRegionResponse(List.of(
                new KakaoRegionResponse.Document("B", "4111113000", "경기도", "수원시 장안구")
        ));

        assertThat(KakaoRegionClient.toRegion(response)).contains(new Region("41111", "경기도 수원시 장안구"));
    }

    @Test
    void 결과가_없거나_코드가_비정상이면_empty() {
        assertThat(KakaoRegionClient.toRegion(null)).isEmpty();
        assertThat(KakaoRegionClient.toRegion(new KakaoRegionResponse(List.of()))).isEmpty();
        assertThat(KakaoRegionClient.toRegion(new KakaoRegionResponse(List.of(
                new KakaoRegionResponse.Document("B", "11", "서울특별시", "강남구"))))).isEmpty();
    }
}
