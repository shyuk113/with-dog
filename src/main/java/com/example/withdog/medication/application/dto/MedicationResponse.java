package com.example.withdog.medication.application.dto;

import com.example.withdog.medication.domain.Medication;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MedicationResponse(
        Long id,
        Long dogId,
        String drugName,
        String dosage,
        String hospitalName,
        LocalDate prescribedDate,
        String memo,
        String imageUrl,
        String rawText,
        LocalDateTime createdAt
) {
    public static MedicationResponse from(Medication medication) {
        return new MedicationResponse(
                medication.getId(),
                medication.getDog().getId(),
                medication.getDrugName(),
                medication.getDosage(),
                medication.getHospitalName(),
                medication.getPrescribedDate(),
                medication.getMemo(),
                medication.getImageUrl(),
                medication.getRawText(),
                medication.getCreatedAt()
        );
    }
}
