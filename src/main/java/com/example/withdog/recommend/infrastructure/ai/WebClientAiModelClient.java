package com.example.withdog.recommend.infrastructure.ai;

import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.recommend.domain.FeatureVector;
import com.example.withdog.recommend.domain.RecommendResult;
import com.example.withdog.recommend.infrastructure.ai.dto.AiRecommendRequest;
import com.example.withdog.recommend.infrastructure.ai.dto.AiRecommendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!local")
public class WebClientAiModelClient implements AiModelClient {

    private final WebClient aiWebClient;

    @Override
    public List<RecommendResult> recommend(FeatureVector featureVector){
        AiRecommendRequest request = new AiRecommendRequest(featureVector.breed(), featureVector.age(), featureVector.weight());

        AiRecommendResponse response;
        try {
            response = aiWebClient.post()
                    .uri("/recommend")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AiRecommendResponse.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();
        } catch (Exception e) {
            log.error("AI 서버 호출 실패:{}", e.getMessage(),e);
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }

        if(response == null || response.courses() == null || response.courses().isEmpty()){
            throw new BusinessException(ErrorCode.AI_SERVER_ERROR);
        }

        return response.courses().stream()
                .map(c-> new RecommendResult(c.courseName(), c.distanceKm(),c.durationMinutes(),c.reason(),c.route()))
                .toList();
    }
}
