package com.example.withdog.medication.infrastructure;

import com.example.withdog.medication.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<Medication, Long> {

    List<Medication> findAllByDogIdOrderByPrescribedDateDesc(Long dogId);

    Optional<Medication> findByIdAndDogId(Long id, Long dogId);
}
