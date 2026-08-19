package com.example.withdog.recommend.infrastructure.ai;

import com.example.withdog.recommend.domain.FeatureVector;
import com.example.withdog.recommend.domain.RecommendResult;
import com.example.withdog.route.domain.RouteStep;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Profile("local")
public class MockAiModelClient implements AiModelClient {

    @Override
    public List<RecommendResult> recommend(FeatureVector featureVector){
        return List.of(
                new RecommendResult("기본 산책 코스", 2.0, 30, "Mock Ai 데이터 - ai 연동 전",List.of(new RouteStep(37.5665, 126.9780))),
                new RecommendResult("공원 순환 코스", 3.5, 50, "품종 특성상 활동량 필요",List.of(new RouteStep(37.5665, 126.9780)))
                );
    }
}
