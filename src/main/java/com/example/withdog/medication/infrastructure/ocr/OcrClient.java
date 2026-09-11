package com.example.withdog.medication.infrastructure.ocr;

public interface OcrClient {

    OcrResult extractMedicationInfo(byte[] imageBytes, String filename);
}
