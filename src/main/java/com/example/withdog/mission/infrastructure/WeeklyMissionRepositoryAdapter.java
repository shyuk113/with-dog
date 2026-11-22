package com.example.withdog.mission.infrastructure;

import com.example.withdog.mission.domain.WeeklyMission;
import com.example.withdog.mission.domain.WeeklyMissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

//도메인 포트 WeeklyMissionRepository의 Spring Data JPA 구현 (저장 기술이 바뀌면 이 어댑터만 교체한다)
@Repository
@RequiredArgsConstructor
public class WeeklyMissionRepositoryAdapter implements WeeklyMissionRepository {

    private final WeeklyMissionJpaRepository weeklyMissionJpaRepository;

    @Override
    public WeeklyMission save(WeeklyMission weeklyMission) {
        return weeklyMissionJpaRepository.save(weeklyMission);
    }

    @Override
    public List<WeeklyMission> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate) {
        return weeklyMissionJpaRepository.findByUserIdAndWeekStartDate(userId, weekStartDate);
    }
}
