package com.example.withdog.recommend.application;

import com.example.withdog.recommend.application.dto.RecommendResponse;
import com.example.withdog.recommend.domain.RecommendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendCacheService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "recommend:dog:";
    private static final Duration TTL = Duration.ofHours(24);

    private String key(Long dogId){
        return KEY_PREFIX + dogId + ":" + LocalDate.now();
    }

    public Optional<List<RecommendResponse>> get(Long dogId){
        String json =  stringRedisTemplate.opsForValue().get(key(dogId));
        if (json == null) {
            return Optional.empty();
        }
        try{
            return Optional.of(objectMapper.readValue(json,new TypeReference<List<RecommendResponse>>() {}));
        } catch (Exception e) {
            log.warn("추천 캐시 역직렬화 실패:{}", e.getMessage());
            return Optional.empty();
        }
    }

    public void save(Long dogId, List<RecommendResponse> responses){
        try{
            stringRedisTemplate.opsForValue().set(key(dogId), objectMapper.writeValueAsString(responses), TTL);
        } catch (Exception e) {
            log.warn("추천 캐시 저장 실패:{}", e.getMessage());
        }
    }

}
