package com.example.withdog.dog.application.dto;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.global.constant.DefaultImages;

import java.time.LocalDate;
import java.util.List;

public record DogResponse(Long id, String name, Breed breed, LocalDate birthDate, Double weight, List<String> healthConditions, String imageUrl) {

    public static DogResponse from(Dog dog){
        String imageUrl = dog.getImageUrl() != null ? dog.getImageUrl() : DefaultImages.DOG_PROFILE;
        return new DogResponse(dog.getId(), dog.getName(), dog.getBreed(), dog.getBirthDate(), dog.getWeight(), dog.getHealthConditions(), imageUrl);
    }
}
