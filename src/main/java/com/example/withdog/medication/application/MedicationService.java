package com.example.withdog.medication.application;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.medication.application.dto.MedicationResponse;
import com.example.withdog.medication.domain.Medication;
import com.example.withdog.medication.infrastructure.MedicationRepository;
import com.example.withdog.medication.infrastructure.ocr.OcrClient;
import com.example.withdog.medication.infrastructure.ocr.OcrResult;
import com.example.withdog.global.infrastructure.storage.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicationService {

    private final MedicationRepository medicationRepository;
    private final DogRepository dogRepository;
    private final OcrClient ocrClient;
    private final ImageStorageService imageStorageService;

    //약 봉투 사진 업로드 -> 텍스트 파싱 -> 약 정보 저장
    @Transactional
    public MedicationResponse scanAndCreate(Long userId, Long dogId, MultipartFile image) {
        Dog dog = dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));

        byte[] imageBytes;
        try {
            imageBytes = image.getBytes();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE);
        }

        String imageUrl = imageStorageService.store(image, "medications");
        OcrResult ocrResult = ocrClient.extractMedicationInfo(imageBytes, image.getOriginalFilename());

        Medication medication = Medication.createFromScan(
                dog,
                ocrResult.drugName(),
                ocrResult.dosage(),
                ocrResult.hospitalName(),
                LocalDate.now(),
                imageUrl,
                ocrResult.rawText()
        );
        medicationRepository.save(medication);
        return MedicationResponse.from(medication);
    }

    //강아지의 복용 약 기록 목록 조회 (다른 병원 방문 시 공유용)
    @Transactional(readOnly = true)
    public List<MedicationResponse> getMedications(Long userId, Long dogId) {
        dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        return medicationRepository.findAllByDogIdOrderByPrescribedDateDesc(dogId).stream()
                .map(MedicationResponse::from)
                .toList();
    }

    //복용 약 기록 상세 조회
    @Transactional(readOnly = true)
    public MedicationResponse getMedication(Long userId, Long dogId, Long id) {
        dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        Medication medication = medicationRepository.findByIdAndDogId(id, dogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_NOT_FOUND));
        return MedicationResponse.from(medication);
    }

    //복용 약 기록 삭제
    @Transactional
    public void deleteMedication(Long userId, Long dogId, Long id) {
        dogRepository.findByUserIdAndId(userId, dogId).orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
        Medication medication = medicationRepository.findByIdAndDogId(id, dogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEDICATION_NOT_FOUND));
        medicationRepository.delete(medication);
    }
}
