package com.example.withdog.dog.application.dto;

import com.example.withdog.dog.domain.Breed;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateDogRequest(
        @NotBlank
        String name,
        @NotNull
        Breed breed,
        @NotNull
        LocalDate birthDate,
        @Min(0)
        @Max(50)
        Double weight) {
}
