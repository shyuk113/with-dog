package com.example.withdog.region.infrastructure;

import com.example.withdog.region.domain.Region;
import com.example.withdog.region.infrastructure.dto.KakaoRegionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!local")
public class KakaoRegionClient implements RegionClient {

    private static final String LEGAL_DONG = "B";

    private final WebClient regionWebClient;

    @Value("${region.api-key:}")
    private String apiKey;

    @Override
    public Optional<Region> findSigungu(double lat, double lon) {
        try {
            KakaoRegionResponse response = regionWebClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/v2/local/geo/coord2regioncode.json")
                            .queryParam("x", lon)
                            .queryParam("y", lat)
                            .build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .bodyToMono(KakaoRegionResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
            return toRegion(response);
        } catch (Exception e) {
            log.error("지역 코드 변환 실패:{}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    //법정동 결과를 우선 사용 (시군구 5자리는 법정동/행정동이 동일하지만, 법정동 코드가 행정 개편에 더 안정적)
    static Optional<Region> toRegion(KakaoRegionResponse response) {
        if (response == null || response.documents() == null || response.documents().isEmpty()) {
            return Optional.empty();
        }
        KakaoRegionResponse.Document doc = response.documents().stream()
                .filter(d -> LEGAL_DONG.equals(d.regionType()))
                .findFirst()
                .orElse(response.documents().get(0));

        if (doc.code() == null || doc.code().length() < Region.SIGUNGU_CODE_LENGTH) {
            return Optional.empty();
        }
        String name = (doc.region1depthName() + " " + doc.region2depthName()).trim();
        return Optional.of(Region.ofSigungu(doc.code(), name));
    }
}
