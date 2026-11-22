package com.example.withdog.mission.domain;

import java.time.LocalDate;
import java.util.List;

//WeeklyMission 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 WeeklyMissionRepositoryAdapter가 담당한다
public interface WeeklyMissionRepository {

    WeeklyMission save(WeeklyMission weeklyMission);

    List<WeeklyMission> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);
}
