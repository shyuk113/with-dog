package com.example.withdog.diet.infrastructure.ocr;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class MockFoodOcrClient implements FoodOcrClient {

    @Override
    public FoodOcrResult extractFoodInfo(byte[] imageBytes, String filename) {
        return new FoodOcrResult(
                "맛있는 오리 저키\n100g당 350kcal\nMock OCR 데이터 - AI 연동 전",
                "맛있는 오리 저키",
                3.5
        );
    }
}
