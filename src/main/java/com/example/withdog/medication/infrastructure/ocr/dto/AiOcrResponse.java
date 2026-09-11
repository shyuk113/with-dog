package com.example.withdog.medication.infrastructure.ocr.dto;

public record AiOcrResponse(String rawText, String drugName, String dosage, String hospitalName) {
}
