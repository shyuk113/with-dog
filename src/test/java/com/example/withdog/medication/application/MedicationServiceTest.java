package com.example.withdog.medication.application;

import com.example.withdog.dog.domain.Breed;
import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.medication.application.dto.MedicationResponse;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class MedicationServiceTest {

    @Autowired
    private MedicationService medicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DogRepository dogRepository;

    private Long userId;
    private Long dogId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.createUserLocal("보호자", "guardian@example.com", "encoded-password", "서울"));
        Dog dog = dogRepository.save(Dog.createDogProfile("콩이", Breed.POODLE, LocalDate.of(2021, 3, 1), 4.2, user));
        userId = user.getId();
        dogId = dog.getId();
    }

    @Test
    void 약봉투_사진을_스캔하면_OCR_결과로_복용기록이_저장된다() {
        MockMultipartFile image = new MockMultipartFile("image", "medicine.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        MedicationResponse response = medicationService.scanAndCreate(userId, dogId, image);

        assertThat(response.id()).isNotNull();
        assertThat(response.dogId()).isEqualTo(dogId);
        assertThat(response.drugName()).isEqualTo("아목시실린 50mg");
        assertThat(response.dosage()).isEqualTo("1일 2회 1정");
        assertThat(response.hospitalName()).isEqualTo("OOO동물병원");
        assertThat(response.rawText()).contains("동물병원");
        assertThat(response.imageUrl()).startsWith("/uploads/medications/");
        assertThat(response.prescribedDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void 강아지의_복용약_기록_목록을_병원_공유용으로_조회할_수_있다() {
        MockMultipartFile image = new MockMultipartFile("image", "medicine.jpg", "image/jpeg", "fake-image-bytes".getBytes());
        medicationService.scanAndCreate(userId, dogId, image);
        medicationService.scanAndCreate(userId, dogId, image);

        List<MedicationResponse> medications = medicationService.getMedications(userId, dogId);

        assertThat(medications).hasSize(2);
    }

    @Test
    void 복용약_기록을_상세조회할_수_있다() {
        MockMultipartFile image = new MockMultipartFile("image", "medicine.jpg", "image/jpeg", "fake-image-bytes".getBytes());
        MedicationResponse created = medicationService.scanAndCreate(userId, dogId, image);

        MedicationResponse found = medicationService.getMedication(userId, dogId, created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.drugName()).isEqualTo("아목시실린 50mg");
    }

    @Test
    void 복용약_기록을_삭제할_수_있다() {
        MockMultipartFile image = new MockMultipartFile("image", "medicine.jpg", "image/jpeg", "fake-image-bytes".getBytes());
        MedicationResponse created = medicationService.scanAndCreate(userId, dogId, image);

        medicationService.deleteMedication(userId, dogId, created.id());

        assertThatThrownBy(() -> medicationService.getMedication(userId, dogId, created.id()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEDICATION_NOT_FOUND);
    }

    @Test
    void 다른_유저의_강아지에는_약기록을_등록할_수_없다() {
        User otherUser = userRepository.save(User.createUserLocal("타인", "other@example.com", "encoded-password", "부산"));
        MockMultipartFile image = new MockMultipartFile("image", "medicine.jpg", "image/jpeg", "fake-image-bytes".getBytes());

        assertThatThrownBy(() -> medicationService.scanAndCreate(otherUser.getId(), dogId, image))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DOG_NOT_FOUND);
    }
}
