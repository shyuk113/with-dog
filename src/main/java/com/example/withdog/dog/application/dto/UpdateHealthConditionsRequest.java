package com.example.withdog.dog.application.dto;

import java.util.List;

public record UpdateHealthConditionsRequest(List<String> healthConditions) {
}
