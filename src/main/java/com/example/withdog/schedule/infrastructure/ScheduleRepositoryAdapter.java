package com.example.withdog.schedule.infrastructure;

import com.example.withdog.schedule.domain.Schedule;
import com.example.withdog.schedule.domain.ScheduleStatus;
import com.example.withdog.schedule.domain.ScheduleType;
import com.example.withdog.schedule.domain.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//도메인 포트 ScheduleRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class ScheduleRepositoryAdapter implements ScheduleRepository {

    private final ScheduleJpaRepository scheduleJpaRepository;

    @Override
    public Schedule save(Schedule schedule) {
        return scheduleJpaRepository.save(schedule);
    }

    @Override
    public void delete(Schedule schedule) {
        scheduleJpaRepository.delete(schedule);
    }

    @Override
    public Optional<Schedule> findByIdAndUserId(Long id, Long userId) {
        return scheduleJpaRepository.findByIdAndUserId(id, userId);
    }

    @Override
    public List<Schedule> findInPeriod(Long userId, LocalDateTime from, LocalDateTime to, Long dogId, ScheduleType type) {
        return scheduleJpaRepository.findInPeriod(userId, from, to, dogId, type);
    }

    @Override
    public List<Schedule> findUpcoming(Long userId, ScheduleStatus status, LocalDateTime now, Pageable pageable) {
        return scheduleJpaRepository.findUpcoming(userId, status, now, pageable);
    }
}
