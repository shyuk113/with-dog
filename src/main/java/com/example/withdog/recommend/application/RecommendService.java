package com.example.withdog.recommend.application;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.recommend.application.dto.RecommendResponse;
import com.example.withdog.recommend.domain.FeatureVector;
import com.example.withdog.recommend.domain.RecommendResult;
import com.example.withdog.recommend.infrastructure.ai.AiModelClient;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import com.example.withdog.weather.application.WeatherService;
import com.example.withdog.weather.domain.WeatherInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final AiModelClient aiModelClient;
    private final DogRepository dogRepository;
    private final RecommendFilterService recommendFilterService;
    private final RecommendCacheService recommendCacheService;
    private final WeatherService weatherService;
    private final UserRepository userRepository;

    public List<RecommendResponse> recommend(Long dogId, Long userId, Double lat, Double lng){
        Dog dog = dogRepository.findByUserIdAndId(userId, dogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean useRegisteredLocation = (lat== null && lng==null);

        if(useRegisteredLocation){
            Optional<List<RecommendResponse>> cached = recommendCacheService.get(dogId);
            if(cached.isPresent()){
                return cached.get();
            }
        }

        double resolvedlat = useRegisteredLocation ? user.getLatitude() : lat;
        double resolvedlon = useRegisteredLocation ? user.getLongitude() : lng;

        WeatherInfo weather = weatherService.getWeather(resolvedlat, resolvedlon);
        FeatureVector fv = FeatureVector.from(dog, resolvedlat, resolvedlon, weather);

        List<RecommendResult> results = aiModelClient.recommend(fv);
        List<RecommendResult> filtered = recommendFilterService.filter(fv, results);
        List<RecommendResponse> responses = filtered.stream().map(RecommendResponse::from).toList();

        if(useRegisteredLocation){
            recommendCacheService.save(dogId, responses);
        }
        return responses;
    }
}
