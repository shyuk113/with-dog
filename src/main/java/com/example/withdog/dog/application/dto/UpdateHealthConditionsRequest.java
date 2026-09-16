package com.example.withdog.dog.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateHealthConditionsRequest(
        @NotNull
        List<String> healthConditions
) {
}
