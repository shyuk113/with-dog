package com.example.withdog.diet.infrastructure.ocr.dto;

public record AiFoodOcrResponse(String rawText, String productName, Double caloriesPerGram) {
}
