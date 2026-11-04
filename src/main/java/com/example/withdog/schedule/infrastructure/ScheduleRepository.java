package com.example.withdog.schedule.infrastructure;

import com.example.withdog.schedule.domain.Schedule;
import com.example.withdog.schedule.domain.ScheduleStatus;
import com.example.withdog.schedule.domain.ScheduleType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByIdAndUserId(Long id, Long userId);

    //캘린더 기간 조회: [from, to) 기간과 겹치는 일정 (10/31~11/1 일정도 11월 조회에 포함)
    //일정 기간도 [startAt, endAt)로 보고 종료 시각은 포함하지 않는다 -> 11/5 종일(~11/6 00:00) 일정은 11/6 조회에 나오지 않음
    //종료 시간이 없는 일정은 시작 시각 한 점으로 본다
    //dogId, type이 null이면 해당 조건은 무시
    @Query("""
            select s from Schedule s
            where s.user.id = :userId
              and s.startAt < :to
              and (s.endAt > :from or (s.endAt is null and s.startAt >= :from))
              and (:dogId is null or s.dog.id = :dogId)
              and (:type is null or s.scheduleType = :type)
            order by s.startAt asc, s.id asc
            """)
    List<Schedule> findInPeriod(@Param("userId") Long userId,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                @Param("dogId") Long dogId,
                                @Param("type") ScheduleType type);

    //다가오는 일정: 아직 끝나지 않은 예정 일정을 가까운 순으로 (진행 중인 일정, 오늘 종일 일정 포함)
    //종료 시간이 있으면 종료 전까지, 없으면 시작 시각 전까지 보인다
    @Query("""
            select s from Schedule s
            where s.user.id = :userId
              and s.scheduleStatus = :status
              and (s.endAt > :now or (s.endAt is null and s.startAt >= :now))
            order by s.startAt asc, s.id asc
            """)
    List<Schedule> findUpcoming(@Param("userId") Long userId,
                                @Param("status") ScheduleStatus status,
                                @Param("now") LocalDateTime now,
                                Pageable pageable);
}
