package com.example.withdog.recommend.application;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.recommend.application.dto.RecommendResponse;
import com.example.withdog.recommend.domain.RecommendResult;
import com.example.withdog.recommend.infrastructure.ai.AiModelClient;
import com.example.withdog.route.application.RouteService;
import com.example.withdog.route.domain.RouteStep;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import com.example.withdog.weather.application.WeatherService;
import com.example.withdog.weather.domain.WeatherInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendServiceTest {

    @Mock
    private AiModelClient aiModelClient;
    @Mock
    private DogRepository dogRepository;
    @Mock
    private RecommendCacheService recommendCacheService;
    @Mock
    private WeatherService weatherService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RouteService routeService;

    private final RecommendFilterService recommendFilterService = new RecommendFilterService();

    private RecommendService recommendService;

    private Dog dog;

    @BeforeEach
    void setUp() {
        recommendService = new RecommendService(aiModelClient, dogRepository, recommendFilterService, recommendCacheService, weatherService, userRepository, routeService);

        User owner = User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울");
        dog = Dog.createDogProfile("콩이", Breed.POODLE, LocalDate.of(2021, 3, 1), 10.0, owner);
    }

    @Test
    void 유저_위치가_등록되지_않은_상태로_좌표없이_추천을_요청하면_명확한_예외를_던진다() {
        when(dogRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(dog));
        User userWithoutLocation = User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울"); // 위치 미설정 (latitude/longitude == null)
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithoutLocation));
        when(recommendCacheService.get(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recommendService.recommend(1L, 1L, null, null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOCATION_REQUIRED);
    }

    @Test
    void 좌표를_직접_전달하면_유저의_등록된_위치가_없어도_정상적으로_추천된다() {
        when(dogRepository.findByUserIdAndId(1L, 1L)).thenReturn(Optional.of(dog));
        User userWithoutLocation = User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울");
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithoutLocation));
        when(weatherService.getWeather(37.5, 127.0)).thenReturn(WeatherInfo.unknown());
        when(aiModelClient.recommend(any())).thenReturn(List.of(new RecommendResult("기본 코스", 1.0, 20, "이유", List.of())));
        when(routeService.getWalkingRoute(any())).thenReturn(List.of(new RouteStep(37.5, 127.0)));

        List<RecommendResponse> responses = recommendService.recommend(1L, 1L, 37.5, 127.0);

        assertThat(responses).hasSize(1);
        verify(recommendCacheService, never()).get(any()); // 좌표 직접 전달 시엔 캐시를 조회하지 않음
    }
}
