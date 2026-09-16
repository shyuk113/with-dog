package com.example.withdog.recommend.domain;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.weather.domain.WeatherInfo;

import java.util.List;

public record FeatureVector(Breed breed, int age, Double weight, Double latitude, Double longitude, WeatherInfo weather, List<String> healthConditions) {

    public static FeatureVector from(Dog dog, Double lat, Double lon, WeatherInfo weather){
        return new FeatureVector(dog.getBreed(), dog.getAge(), dog.getWeight(), lat, lon, weather, dog.getHealthConditions());
    }
}
