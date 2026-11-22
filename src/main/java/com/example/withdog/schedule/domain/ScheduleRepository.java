package com.example.withdog.schedule.domain;

import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//Schedule 저장소 포트: 서비스는 저장 기술(JPA 등)을 모르고 이 인터페이스에만 의존한다
//구현은 infrastructure의 ScheduleRepositoryAdapter가 담당한다
public interface ScheduleRepository {

    Schedule save(Schedule schedule);

    void delete(Schedule schedule);

    //소유권을 조회 조건에 포함 (남의 일정은 조회되지 않음)
    Optional<Schedule> findByIdAndUserId(Long id, Long userId);

    //캘린더 기간 조회: [from, to)와 겹치는 일정, dogId/type이 null이면 조건 무시
    List<Schedule> findInPeriod(Long userId, LocalDateTime from, LocalDateTime to, Long dogId, ScheduleType type);

    //다가오는 일정: 아직 끝나지 않은 일정을 가까운 순으로
    List<Schedule> findUpcoming(Long userId, ScheduleStatus status, LocalDateTime now, Pageable pageable);
}
