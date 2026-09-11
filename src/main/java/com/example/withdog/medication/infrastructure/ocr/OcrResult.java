package com.example.withdog.medication.infrastructure.ocr;

public record OcrResult(String rawText, String drugName, String dosage, String hospitalName) {
}
