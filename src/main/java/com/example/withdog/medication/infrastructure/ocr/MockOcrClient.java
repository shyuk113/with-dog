package com.example.withdog.medication.infrastructure.ocr;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class MockOcrClient implements OcrClient {

    @Override
    public OcrResult extractMedicationInfo(byte[] imageBytes, String filename) {
        return new OcrResult(
                "OOO동물병원\n반려견: 콩이\n약품명: 아목시실린 50mg\n용법: 1일 2회 1정씩 7일분\nMock OCR 데이터 - AI 연동 전",
                "아목시실린 50mg",
                "1일 2회 1정",
                "OOO동물병원"
        );
    }
}
