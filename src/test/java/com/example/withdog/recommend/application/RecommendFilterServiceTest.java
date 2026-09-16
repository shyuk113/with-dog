package com.example.withdog.recommend.application;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.recommend.domain.FeatureVector;
import com.example.withdog.recommend.domain.RecommendResult;
import com.example.withdog.weather.domain.Condition;
import com.example.withdog.weather.domain.WeatherInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendFilterServiceTest {

    private final RecommendFilterService filterService = new RecommendFilterService();

    private RecommendResult course(String name, double distanceKm) {
        return new RecommendResult(name, distanceKm, 30, "기본 추천 코스", List.of());
    }

    @Test
    void 건강한_성견은_장거리_코스도_그대로_추천받는다() {
        FeatureVector fv = new FeatureVector(Breed.GOLDEN_RETRIEVER, 3, 20.0, 37.5, 127.0, WeatherInfo.unknown(), List.of());

        List<RecommendResult> filtered = filterService.filter(fv, List.of(course("장거리 코스", 3.5)));

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).reason()).isEqualTo("기본 추천 코스");
    }

    @Test
    void 관절염이_있으면_장거리_코스가_제외되고_사유에_안내가_붙는다() {
        FeatureVector fv = new FeatureVector(Breed.POODLE, 3, 6.0, 37.5, 127.0, WeatherInfo.unknown(), List.of("관절염"));

        List<RecommendResult> filtered = filterService.filter(fv, List.of(course("단거리 코스", 1.5), course("장거리 코스", 3.5)));

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).courseName()).isEqualTo("단거리 코스");
        assertThat(filtered.get(0).reason()).contains("관절염").contains("건강상태를 고려");
    }

    @Test
    void 비만인_강아지도_관절염과_동일하게_장거리_코스에서_제외된다() {
        FeatureVector fv = new FeatureVector(Breed.BEAGLE, 4, 15.0, 37.5, 127.0, WeatherInfo.unknown(), List.of("비만"));

        List<RecommendResult> filtered = filterService.filter(fv, List.of(course("장거리 코스", 2.5)));

        assertThat(filtered).isEmpty();
    }

    @Test
    void 소형견이나_노령견_조건과_지병_조건이_동일한_거리제한을_공유한다() {
        FeatureVector smallDog = new FeatureVector(Breed.CHIHUAHUA, 3, 2.5, 37.5, 127.0, WeatherInfo.unknown(), List.of());

        List<RecommendResult> filtered = filterService.filter(smallDog, List.of(course("장거리 코스", 2.5)));

        assertThat(filtered).isEmpty();
    }

    @Test
    void 지병과_우천_안내가_동시에_있으면_사유에_둘_다_포함된다() {
        WeatherInfo rainy = new WeatherInfo(Condition.RAIN, 18.0);
        FeatureVector fv = new FeatureVector(Breed.POODLE, 3, 6.0, 37.5, 127.0, rainy, List.of("심장질환"));

        List<RecommendResult> filtered = filterService.filter(fv, List.of(course("단거리 코스", 1.0)));

        assertThat(filtered.get(0).reason()).contains("우비 착용").contains("심장질환");
    }
}
