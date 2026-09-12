package com.example.withdog.mission.infrastructure;

import com.example.withdog.mission.domain.WeeklyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyMissionRepository extends JpaRepository<WeeklyMission, Long> {

    List<WeeklyMission> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);
}
