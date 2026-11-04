package com.example.withdog.schedule.application;

import com.example.withdog.dog.domain.Dog;
import com.example.withdog.dog.infrastructure.DogRepository;
import com.example.withdog.global.exception.BusinessException;
import com.example.withdog.global.exception.ErrorCode;
import com.example.withdog.schedule.application.dto.request.CreateScheduleRequest;
import com.example.withdog.schedule.application.dto.request.UpdateScheduleRequest;
import com.example.withdog.schedule.application.dto.request.UpdateStatus;
import com.example.withdog.schedule.application.dto.response.ScheduleResponse;
import com.example.withdog.schedule.domain.Schedule;
import com.example.withdog.schedule.domain.ScheduleStatus;
import com.example.withdog.schedule.domain.ScheduleType;
import com.example.withdog.schedule.infrastructure.ScheduleRepository;
import com.example.withdog.user.domain.User;
import com.example.withdog.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    public static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    public static final int MAX_PERIOD_DAYS = 62;  //한 번에 조회 가능한 최대 기간 (두 달)
    public static final int MAX_UPCOMING = 20;

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final DogRepository dogRepository;

    //일정 생성
    @Transactional
    public ScheduleResponse createSchedule(CreateScheduleRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));
        boolean allDay = Boolean.TRUE.equals(request.allDay()); //null을 primitive로 언박싱하지 않도록 명시적으로 변환
        validateTime(request.startAt(), request.endAt(), allDay);

        Schedule schedule = Schedule.createSchedule(user,
                findMyDog(request.dogId(), userId),
                request.type(),
                request.title(),
                request.placeName(),
                request.memo(),
                request.startAt(),
                request.endAt(),
                allDay);
        scheduleRepository.save(schedule);
        return ScheduleResponse.from(schedule);
    }

    //캘린더 조회: from~to(날짜, 양 끝 포함) 기간과 겹치는 일정
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long userId, LocalDate from, LocalDate to, Long dogId, ScheduleType type) {
        if (from == null || to == null || to.isBefore(from) || ChronoUnit.DAYS.between(from, to) >= MAX_PERIOD_DAYS) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_PERIOD);
        }
        return scheduleRepository.findInPeriod(userId, from.atStartOfDay(), to.plusDays(1).atStartOfDay(), dogId, type)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    //다가오는 일정 (아직 끝나지 않은 예정 일정, 가까운 순)
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getUpcomingSchedules(Long userId, int limit) {
        return scheduleRepository.findUpcoming(userId, ScheduleStatus.SCHEDULED, LocalDateTime.now(ZONE),
                        PageRequest.of(0, Math.clamp(limit, 1, MAX_UPCOMING)))
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    //일정 상세
    @Transactional(readOnly = true)
    public ScheduleResponse getSchedule(Long id, Long userId) {
        return ScheduleResponse.from(findMySchedule(id, userId));
    }

    //일정 변경
    @Transactional
    public ScheduleResponse updateSchedule(UpdateScheduleRequest request, Long id , Long userId) {
        Schedule schedule = findMySchedule(id, userId);
        boolean allDay = Boolean.TRUE.equals(request.allDay());
        validateTime(request.startAt(), request.endAt(), allDay);
        schedule.updateSchedule(findMyDog(request.dogId(), userId), request.type(), request.title(), request.placeName(),
                request.memo(), request.startAt(), request.endAt(), allDay);
        return ScheduleResponse.from(schedule);
    }

    //상태 변경 (다녀옴 / 취소)
    @Transactional
    public ScheduleResponse updateScheduleStatus(UpdateStatus request, Long id, Long userId) {
        Schedule schedule = findMySchedule(id, userId);
        schedule.updateStatus(request.status());
        return ScheduleResponse.from(schedule);
    }

    //일정 삭제
    @Transactional
    public void deleteSchedule(Long id, Long userId) {
        scheduleRepository.delete(findMySchedule(id, userId));
    }

    //소유권을 조회 조건에 포함해 남의 일정은 "없는 일정"으로 취급 (IDOR 방지)
    private Schedule findMySchedule(Long id, Long userId) {
        return scheduleRepository.findByIdAndUserId(id, userId)
                .orElseThrow(()-> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }

    //강아지는 선택 항목. 지정했다면 내 강아지여야 한다
    private Dog findMyDog(Long dogId, Long userId) {
        if (dogId == null) {
            return null;
        }
        return dogRepository.findByUserIdAndId(userId, dogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOG_NOT_FOUND));
    }

    //종료가 없으면 검사하지 않는다. 종일 일정은 시간을 버리고 날짜로만 비교한다 (같은 날이면 시간이 앞서도 허용)
    private void validateTime(LocalDateTime startAt, LocalDateTime endAt, boolean allDay) {
        if (endAt == null) {
            return;
        }
        boolean endsBeforeStart = allDay
                ? endAt.toLocalDate().isBefore(startAt.toLocalDate())
                : endAt.isBefore(startAt);
        if (endsBeforeStart) {
            throw new BusinessException(ErrorCode.INVALID_SCHEDULE_TIME);
        }
    }
}
