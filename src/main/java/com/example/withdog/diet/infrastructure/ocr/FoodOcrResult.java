package com.example.withdog.diet.infrastructure.ocr;

public record FoodOcrResult(String rawText, String productName, Double caloriesPerGram) {
}
