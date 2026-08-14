package com.example.withdog.recommend.domain;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;

public record FeatureVector(Breed breed, int age, Double weight) {

    public static FeatureVector from(Dog dog){
        return new FeatureVector(dog.getBreed(), dog.getAge(), dog.getWeight());
    }
}
