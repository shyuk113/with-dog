package com.example.withdog.recommend.infrastructure.ai;

import com.example.withdog.recommend.domain.FeatureVector;
import com.example.withdog.recommend.domain.RecommendResult;

import java.util.List;

public interface AiModelClient {
    List<RecommendResult> recommend(FeatureVector featureVector);
}
