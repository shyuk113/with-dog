package com.example.withdog.medication.infrastructure;

import com.example.withdog.medication.domain.Medication;
import com.example.withdog.medication.domain.MedicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//도메인 포트 MedicationRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class MedicationRepositoryAdapter implements MedicationRepository {

    private final MedicationJpaRepository medicationJpaRepository;

    @Override
    public Medication save(Medication medication) {
        return medicationJpaRepository.save(medication);
    }

    @Override
    public void delete(Medication medication) {
        medicationJpaRepository.delete(medication);
    }

    @Override
    public List<Medication> findAllByDogIdOrderByPrescribedDateDesc(Long dogId) {
        return medicationJpaRepository.findAllByDogIdOrderByPrescribedDateDesc(dogId);
    }

    @Override
    public Optional<Medication> findByIdAndDogId(Long id, Long dogId) {
        return medicationJpaRepository.findByIdAndDogId(id, dogId);
    }
}
