package com.example.withdog.medication.presentation;

import com.example.withdog.medication.application.MedicationService;
import com.example.withdog.medication.application.dto.MedicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/dogs/{dogId}/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationService medicationService;

    //약 봉투 사진 업로드 -> 텍스트 파싱 -> 저장
    @PostMapping(value = "/scan", consumes = "multipart/form-data")
    public ResponseEntity<MedicationResponse> scanAndCreate(@PathVariable Long dogId,
                                                              @RequestParam("image") MultipartFile image,
                                                              @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicationService.scanAndCreate(userId, dogId, image));
    }

    //복용 약 기록 목록 조회 (다른 병원 방문 시 공유용)
    @GetMapping
    public ResponseEntity<List<MedicationResponse>> getMedications(@PathVariable Long dogId, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(medicationService.getMedications(userId, dogId));
    }

    //복용 약 기록 상세 조회
    @GetMapping("/{id}")
    public ResponseEntity<MedicationResponse> getMedication(@PathVariable Long dogId, @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(medicationService.getMedication(userId, dogId, id));
    }

    //복용 약 기록 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedication(@PathVariable Long dogId, @PathVariable Long id, @AuthenticationPrincipal Long userId) {
        medicationService.deleteMedication(userId, dogId, id);
        return ResponseEntity.noContent().build();
    }
}
