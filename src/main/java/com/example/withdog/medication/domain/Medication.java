package com.example.withdog.medication.domain;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Medication extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dog_id", nullable = false)
    private Dog dog;

    private String drugName;

    private String dosage;

    private String hospitalName;

    private LocalDate prescribedDate;

    private String memo;

    private String imageUrl;

    @Lob
    private String rawText;

    @Builder
    private Medication(Dog dog, String drugName, String dosage, String hospitalName, LocalDate prescribedDate, String imageUrl, String rawText) {
        this.dog = dog;
        this.drugName = drugName;
        this.dosage = dosage;
        this.hospitalName = hospitalName;
        this.prescribedDate = prescribedDate;
        this.imageUrl = imageUrl;
        this.rawText = rawText;
    }

    public static Medication createFromScan(Dog dog, String drugName, String dosage, String hospitalName, LocalDate prescribedDate, String imageUrl, String rawText) {
        return Medication.builder()
                .dog(dog)
                .drugName(drugName)
                .dosage(dosage)
                .hospitalName(hospitalName)
                .prescribedDate(prescribedDate)
                .imageUrl(imageUrl)
                .rawText(rawText)
                .build();
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }
}
