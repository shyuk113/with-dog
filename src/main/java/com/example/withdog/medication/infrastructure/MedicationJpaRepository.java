package com.example.withdog.medication.infrastructure;

import com.example.withdog.medication.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 MedicationRepository에 의존한다
public interface MedicationJpaRepository extends JpaRepository<Medication, Long> {

    List<Medication> findAllByDogIdOrderByPrescribedDateDesc(Long dogId);

    Optional<Medication> findByIdAndDogId(Long id, Long dogId);
}
