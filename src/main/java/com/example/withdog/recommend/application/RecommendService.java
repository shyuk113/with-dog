package com.example.withdog.recommend.application;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.recommend.application.dto.RecommendResponse;
import com.example.withdog.recommend.domain.FeatureVector;
import com.example.withdog.recommend.domain.RecommendResult;
import com.example.withdog.recommend.infrastructure.ai.AiModelClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final AiModelClient aiModelClient;
    private final DogRepository dogRepository;
    
    public List<RecommendResponse> recommend(Long dogId, Long userId){
        Dog dog = dogRepository.findByUserIdAndId(userId, dogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));

        FeatureVector featureVector = FeatureVector.from(dog);
        List<RecommendResult> results = aiModelClient.recommend(featureVector);

        return results.stream().map(RecommendResponse::from).toList();
    }
}
