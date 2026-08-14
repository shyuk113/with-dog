package com.example.withdog.recommend.infrastructure.ai.dto;

import com.example.withdog.dog.domain.Breed;

public record AiRecommendRequest(Breed breed, int age, Double weight) {
}
