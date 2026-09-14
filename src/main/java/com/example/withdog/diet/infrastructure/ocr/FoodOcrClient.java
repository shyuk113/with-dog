package com.example.withdog.diet.infrastructure.ocr;

public interface FoodOcrClient {

    FoodOcrResult extractFoodInfo(byte[] imageBytes, String filename);
}
