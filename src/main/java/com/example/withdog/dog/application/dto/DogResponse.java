package com.example.withdog.dog.application.dto;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;

import java.time.LocalDate;

public record DogResponse(Long id, String name, Breed breed, LocalDate birthDate, Double weight) {

    public static DogResponse from(Dog dog){
        return new DogResponse(dog.getId(), dog.getName(),  dog.getBreed(), dog.getBirthDate(), dog.getWeight());
    }
}
