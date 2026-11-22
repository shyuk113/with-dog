package com.example.withdog.medication.domain;

import java.util.List;
import java.util.Optional;

//Medication 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 MedicationRepositoryAdapter가 담당한다
public interface MedicationRepository {

    Medication save(Medication medication);

    void delete(Medication medication);

    List<Medication> findAllByDogIdOrderByPrescribedDateDesc(Long dogId);

    Optional<Medication> findByIdAndDogId(Long id, Long dogId);
}
