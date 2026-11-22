package com.example.withdog.mission.infrastructure;

import com.example.withdog.mission.domain.WeeklyMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

//Spring Data JPA 구현 기술. 서비스는 이 인터페이스가 아니라 domain의 WeeklyMissionRepository에 의존한다
public interface WeeklyMissionJpaRepository extends JpaRepository<WeeklyMission, Long> {

    List<WeeklyMission> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);
}
